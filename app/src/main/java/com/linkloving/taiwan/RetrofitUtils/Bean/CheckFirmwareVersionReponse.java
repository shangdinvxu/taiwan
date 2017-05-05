package com.linkloving.taiwan.RetrofitUtils.Bean;

import java.io.Serializable;

/**
 * Created by Daniel.Xu on 2017/2/19.
 */

public class CheckFirmwareVersionReponse implements Serializable {


    /**
     * version_code : 0514
     * model_name : B100D2
     * file_name : TRKR360_0412_170428.zip
     * return_update_log : TEST1
     */

    private String version_code;
    private String model_name;
    private String file_name;
    private String return_update_log;

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setReturn_update_log(String return_update_log) {
        this.return_update_log = return_update_log;
    }

    public String getVersion_code() {
        return version_code;
    }

    public String getModel_name() {
        return model_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getReturn_update_log() {
        return return_update_log;
    }
}
