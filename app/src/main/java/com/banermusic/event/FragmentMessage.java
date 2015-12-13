package com.banermusic.event;

/**
 * Created by kodywu on 28/9/15.
 */
public class FragmentMessage {
    /**
     * 切换标志
     */
    public final static int HOMETOSCENE = 0;
    public final static int SCENETOMUSICLIST = 1;
    public final static int MUSICLISTTOPLAYER = 2;
    public final static int HOMETOPLAYER = 3;
    public final static int PLAYERTOHOME = 4;
    public final static int SCENETOCURRENTLIST = 5;
    public final static int REFRESH = 98;
    public final static int DATAMESSAGE = 99;

    public FragmentMessage(int type){
        this.type = type;
    }

    public FragmentMessage(int type, Object data){
        this.type = type;
        this.data = data;
    }

    private int type;
    private Object data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
