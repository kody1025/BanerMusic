package com.banermusic.bean;

import java.io.Serializable;

/**
 * Created by kodywu on 25/5/15.
 */
public class BaseBean implements Serializable {
    /**
     * 返回代码，-1 错误，0 成功，100用户没有登录
     */
    private String ret_code;
    /**
     * 返回信息
     */
    private String ret_msg;

    public String getRet_code() {
        return ret_code;
    }

    public void setRet_code(String ret_code) {
        this.ret_code = ret_code;
    }

    public String getRet_msg() {
        return ret_msg;
    }

    public void setRet_msg(String ret_msg) {
        this.ret_msg = ret_msg;
    }
}
