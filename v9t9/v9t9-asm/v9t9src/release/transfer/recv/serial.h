

int	com_init(int port, unsigned baudrate, char irq);
int	com_send(unsigned char ch);
unsigned com_read(void);
int	buf_init(void);
void	interrupt com_int(void);
void	com_off(void);
