
/*
 * ******************************************************************************
 *   Copyright (c) 2014-2015 Ivan Gaglioti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
package org.symptomcheck.capstone.bus;

/**
 * Created by Ivan on 26/10/2014.
 */
public class DownloadEvent {
    private boolean status;
    private int valueEvent;

    public DownloadEvent(boolean status, int valueEvnt){
        this.status = status;
        this.valueEvent = valueEvnt;
    }


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getValueEvent() {
        return valueEvent;
    }

    public void setValueEvent(int valueEvent) {
        this.valueEvent = valueEvent;
    }

    public static class Builder{
        boolean status;
        private int valueEvnt;
        public Builder setStatus(boolean status){
            this.status = status;
            return this;
        }

        public Builder setValueEvnt(int valueEvnt){
            this.valueEvnt = valueEvnt;
            return this;
        }

        public DownloadEvent Build(){
            return new DownloadEvent(this.status,this.valueEvnt);
        }
    }

    @Override
    public String toString(){
        return "status:" + isStatus() + ";" + "valueEvent:" + this.valueEvent;
    }
}
