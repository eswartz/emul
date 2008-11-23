/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.WordMemoryArea;

public class ConsoleMemoryArea extends WordMemoryArea {
    ConsoleMemoryArea() {
        bWordAccess = true;
    }
}