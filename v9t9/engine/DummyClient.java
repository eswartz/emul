package v9t9.engine;



public class DummyClient implements Client {
    v9t9.engine.VdpHandler video;
    v9t9.engine.SoundHandler sound;
    private CruHandler cru;
    
    public DummyClient() {
        video = new v9t9.engine.VdpHandler() {

            public void writeVdpReg(byte reg, byte val) {
            }

            public byte readVdpStatus() {
                return 0;
            }

            public void writeVdpMemory(short vdpaddr, byte val) {
            }
            
            public void update() {
            }
        };
        sound = new v9t9.engine.SoundHandler() {
            public void writeSound(byte val) {
            }
        };
        cru = new CruHandler() {

            public void writeBits(int addr, int val, int num) {
                
            }

            public int readBits(int addr, int num) {
                return 0;
            }
            
        };
    }

    /* (non-Javadoc)
     * @see v9t9.Client#getVideo()
     */
    public VdpHandler getVideoHandler() {
        // TODO Auto-generated method stub
        return video;
    }

    /* (non-Javadoc)
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideoHandler(VdpHandler video) {
        // TODO Auto-generated method stub
        this.video = video;
    }
    
    /* (non-Javadoc)
     * @see v9t9.Client#close()
     */
    public void close() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see v9t9.Client#timerTick()
     */
    public void timerInterrupt() {
        // TODO Auto-generated method stub
        
    }

    public SoundHandler getSoundHandler() {
        return sound;
    }
    public void setSoundHandler(SoundHandler sound) {
        this.sound = sound;
    }

    public CruHandler getCruHandler() {
        return cru;
    }

    public void setCruHandler(CruHandler handler) {
        this.cru = handler;
    }
}
