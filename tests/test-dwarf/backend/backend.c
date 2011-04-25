#include <config.h>

#include <sys/stat.h>
#include <assert.h>

#include <framework/context.h>
#include <framework/events.h>
#include <framework/myalloc.h>

#include <services/tcf_elf.h>
#include <services/symbols.h>
#include <services/linenumbers.h>
#include <services/memorymap.h>

#include <backend/backend.h>

static Context * elf_ctx = NULL;
static MemoryMap mem_map;
static RegisterDefinition reg_defs[MAX_REGS];
static char reg_names[MAX_REGS][32];
static uint8_t reg_vals[MAX_REGS * 8];
static unsigned reg_size = 0;

static uint8_t frame_data[0x1000];
static ContextAddress frame_addr = 0x40000000u;

#define MAX_HEADERS 17
static const char * elf_file_name = NULL;
static ELF_PHeader elf_headers[MAX_HEADERS];
static int elf_headers_cnt = 0;
static int elf_headers_pos = 0;
static ContextAddress pc = 0;
static unsigned pass_cnt = 0;
static int test_posted = 0;
static struct timespec time_start;

RegisterDefinition * get_reg_definitions(Context * ctx) {
    return reg_defs;
}

RegisterDefinition * get_PC_definition(Context * ctx) {
    return reg_defs;
}

Context * id2ctx(const char * id) {
    if (id != NULL && strcmp(id, elf_ctx->id) == 0) return elf_ctx;
    return NULL;
}

unsigned context_word_size(Context * ctx) {
    return get_PC_definition(ctx)->size;
}

int context_has_state(Context * ctx) {
    return 1;
}

Context * context_get_group(Context * ctx, int group) {
    return ctx;
}

int context_read_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    if (ctx != elf_ctx) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    memcpy(buf, reg_vals + def->offset + offs, size);
    return 0;
}

int context_write_reg(Context * ctx, RegisterDefinition * def, unsigned offs, unsigned size, void * buf) {
    if (ctx != elf_ctx) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    memcpy(reg_vals + def->offset + offs, buf, size);
    return 0;
}

int context_read_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    if (address >= frame_addr && address + size <= frame_addr + sizeof(frame_data)) {
        memcpy(buf, frame_data + (address - frame_addr), size);
        return 0;
    }
    /* TODO: context_read_mem */
    errno = ERR_UNSUPPORTED;
    return -1;
}

int context_write_mem(Context * ctx, ContextAddress address, void * buf, size_t size) {
    /* TODO: context_write_mem */
    errno = ERR_UNSUPPORTED;
    return -1;
}

int context_get_memory_map(Context * ctx, MemoryMap * map) {
    unsigned i;
    for (i = 0; i < mem_map.region_cnt; i++) {
        MemoryRegion * r = NULL;
        if (map->region_cnt >= map->region_max) {
            map->region_max += 8;
            map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
        }
        r = map->regions + map->region_cnt++;
        *r = mem_map.regions[i];
        r->file_name = loc_strdup(r->file_name);
    }
    return 0;
}

int crawl_stack_frame(StackFrame * frame, StackFrame * down) {
    if (frame->is_top_frame) {
        frame->fp = frame_addr;
        return 0;
    }
    errno = ERR_INV_ADDRESS;
    return -1;
}

static void error(const char * func) {
    int err = errno;
    printf("File    : %s\n", elf_file_name);
    printf("Address : 0x%" PRIX64 "\n", (uint64_t)pc);
    printf("Function: %s\n", func);
    printf("Error   : %s\n", errno_to_str(err));
    fflush(stdout);
    exit(1);
}

static void line_numbers_callback(CodeArea * area, void * args) {
    CodeArea * dst = (CodeArea *)args;
    *dst = *area;
}

static const char * files[] = {
    "files/hypervisor",
    "files/vxWorks-1",
    "files/vxWorks-2",
    "files/vxWorks-3",
    //"files/philosophers.vxe.diab",
    "files/philosophers.vxe.icc",
    // "files/vxWorks.diab",
    "files/rule30-threaded.vxe",
    "files/vxWorks.icc",
    "files/cbcp-main-ppc.x",
    "files/cbcp-main-x86",
    "files/Gilbarco.elf"
};

static void print_time(struct timespec time_start, int cnt) {
    struct timespec time_now;
    struct timespec time_diff;
    if (cnt == 0) return;
    clock_gettime(CLOCK_REALTIME, &time_now);
    time_diff.tv_sec = time_now.tv_sec - time_start.tv_sec;
    if (time_now.tv_nsec < time_start.tv_nsec) {
        time_diff.tv_sec--;
        time_diff.tv_nsec = time_now.tv_nsec + 1000000000 - time_start.tv_nsec;
    }
    else {
        time_diff.tv_nsec = time_now.tv_nsec - time_start.tv_nsec;
    }
    time_diff.tv_nsec /= cnt;
    time_diff.tv_nsec += (long)(((uint64_t)(time_diff.tv_sec % cnt) * 1000000000) / cnt);
    time_diff.tv_sec /= cnt;
    printf("search time: %ld.%09ld\n", (long)time_diff.tv_sec, time_diff.tv_nsec);
    fflush(stdout);
}

static void test(void * args);

static void next_pc(void) {
    Symbol * sym = NULL;
    CodeArea area;
    struct timespec time_now;
    int test_cnt = 0;

    for (;;) {
        if (elf_headers_pos < 0) {
            elf_headers_pos = 0;
            pc = elf_headers[0].address;
        }
        else if (pc + 5 < elf_headers[elf_headers_pos].address + elf_headers[elf_headers_pos].file_size) {
            pc += 5;
        }
        else if (elf_headers_pos + 1 < elf_headers_cnt) {
            elf_headers_pos++;
            pc = elf_headers[elf_headers_pos].address;
        }
        else {
            elf_headers_pos++;
            pc = 0;
            print_time(time_start, test_cnt);
            return;
        }

        set_regs_PC(elf_ctx, pc);
        send_context_changed_event(elf_ctx);
#if 0
        switch (pc % 4) {
        case 0: name = "???"; break;
        case 1: name = "run_undetached"; break;
        case 2: name = "set_m_bAbort"; break;
        case 3: name = "deallocate_buffers"; break;
        }
#endif
        if (find_symbol_by_addr(elf_ctx, STACK_NO_FRAME, pc, &sym) < 0) {
            if (get_error_code(errno) != ERR_SYM_NOT_FOUND) {
                error("find_symbol_by_addr");
            }
        }
        else {
            char * name = NULL;
            char name_buf[0x1000];
            if (get_symbol_name(sym, &name) < 0) {
                error("get_symbol_name");
            }
            if (name != NULL) {
                strcpy(name_buf, name);
                if (find_symbol_by_addr(elf_ctx, STACK_TOP_FRAME, pc, &sym) < 0) {
                    error("find_symbol_by_addr");
                }
                if (get_symbol_name(sym, &name) < 0) {
                    error("get_symbol_name");
                }
                if (strcmp(name_buf, name) != 0) {
                    errno = ERR_OTHER;
                    error("strcmp(name_buf, name)");
                }
                if (find_symbol_by_name(elf_ctx, STACK_TOP_FRAME, 0, name_buf, &sym) < 0) {
                    if (get_error_code(errno) != ERR_SYM_NOT_FOUND) {
                        error("find_symbol_by_name");
                    }
                }
            }
        }
        memset(&area, 0, sizeof(area));
        if (address_to_line(elf_ctx, pc, pc + 1, line_numbers_callback, &area) < 0) {
            error("address_to_line");
        }
        else if (area.start_line > 0) {
            char elf_file_name[0x1000];
            strlcpy(elf_file_name, area.file, sizeof(elf_file_name));
            if (line_to_address(elf_ctx, elf_file_name, area.start_line, area.start_column, line_numbers_callback, &area) < 0) {
                error("line_to_address");
            }
        }
        test_cnt++;
        if (elf_headers_pos == 0 && pc == elf_headers[0].address) {
            struct timespec time_diff;
            clock_gettime(CLOCK_REALTIME, &time_now);
            time_diff.tv_sec = time_now.tv_sec - time_start.tv_sec;
            if (time_now.tv_nsec < time_start.tv_nsec) {
                time_diff.tv_sec--;
                time_diff.tv_nsec = time_now.tv_nsec + 1000000000 - time_start.tv_nsec;
            }
            else {
                time_diff.tv_nsec = time_now.tv_nsec - time_start.tv_nsec;
            }
            printf("load time: %ld.%09ld\n", (long)time_diff.tv_sec, time_diff.tv_nsec);
            fflush(stdout);
            time_start = time_now;
        }
        else if (test_cnt >= 100000) {
            print_time(time_start, test_cnt);
            clock_gettime(CLOCK_REALTIME, &time_start);
            test_posted = 1;
            post_event(test, NULL);
            return;
        }
    }
}

static void next_file(void) {
    unsigned j;
    ELF_File * f = NULL;
    struct stat st;

    elf_file_name = files[pass_cnt % (sizeof(files) / sizeof(char *))];

    printf("File: %s\n", elf_file_name);
    fflush(stdout);
    if (stat(elf_file_name, &st) < 0) {
        printf("Cannot stat ELF: %s\n", errno_to_str(errno));
        exit(1);
    }

    clock_gettime(CLOCK_REALTIME, &time_start);

    f = elf_open(elf_file_name);;
    if (f == NULL) {
        printf("Cannot open ELF: %s\n", errno_to_str(errno));
        exit(1);
    }

    if (elf_ctx == NULL) {
        elf_ctx = create_context("test");
        elf_ctx->stopped = 1;
        elf_ctx->pending_intercept = 1;
        elf_ctx->mem = elf_ctx;
        elf_ctx->big_endian = f->big_endian;
        list_add_first(&elf_ctx->ctxl, &context_root);
        elf_ctx->ref_count++;
    }

    context_clear_memory_map(&mem_map);
    for (j = 0; j < f->pheader_cnt; j++) {
        MemoryRegion * r = NULL;
        ELF_PHeader * p = f->pheaders + j;
        if (p->type != PT_LOAD) continue;
        if (mem_map.region_cnt >= mem_map.region_max) {
            mem_map.region_max += 8;
            mem_map.regions = (MemoryRegion *)loc_realloc(mem_map.regions, sizeof(MemoryRegion) * mem_map.region_max);
        }
        r = mem_map.regions + mem_map.region_cnt++;
        memset(r, 0, sizeof(MemoryRegion));
        r->addr = p->address;
        r->file_name = loc_strdup(elf_file_name);
        r->file_offs = p->offset;
        r->size = p->file_size;
        r->flags = MM_FLAG_R | MM_FLAG_W | MM_FLAG_X;
        r->dev = st.st_dev;
        r->ino = st.st_ino;
    }
    memory_map_event_module_loaded(elf_ctx);

    reg_size = 0;
    memset(reg_defs, 0, sizeof(reg_defs));
    memset(reg_vals, 0, sizeof(reg_vals));
    for (j = 0; j < MAX_REGS - 1; j++) {
        RegisterDefinition * r = reg_defs + j;
        r->big_endian = f->big_endian;
        r->dwarf_id = j == 0 ? -1 : j - 1;
        r->eh_frame_id = -1;
        r->name = reg_names[j];
        snprintf(reg_names[j], sizeof(reg_names[j]), "R%d", j);
        r->offset = reg_size;
        r->size = f->elf64 ? 8 : 4;
        if (j == 0) r->role = "PC";
        reg_size += r->size;
    }

    elf_headers_pos = -1;
    elf_headers_cnt = 0;
    for (j = 0; j < f->pheader_cnt && elf_headers_cnt < MAX_HEADERS; j++) {
        ELF_PHeader * p = f->pheaders + j;
        if (p->type != PT_LOAD) continue;
        if ((p->flags & PF_X) == 0) continue;
        elf_headers[elf_headers_cnt++] = *p;
    }

    pc = 0;
    pass_cnt++;
    test_posted = 1;
    post_event(test, NULL);
}

static void test(void * args) {
    assert(test_posted);
    test_posted = 0;
    if (elf_file_name == NULL || elf_headers_pos >= elf_headers_cnt) {
        next_file();
    }
    else {
        next_pc();
    }
}

static void on_elf_file_closed(ELF_File * f) {
    if (!test_posted) {
        test_posted = 1;
        post_event(test, NULL);
    }
}

void init_contexts_sys_dep(void) {
    elf_add_close_listener(on_elf_file_closed);
    test_posted = 1;
    post_event(test, NULL);
}
