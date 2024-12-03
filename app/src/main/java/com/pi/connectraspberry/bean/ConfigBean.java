package com.pi.connectraspberry.bean;

public class ConfigBean {

    private int seconds;//播放时长
    private int hue;//色调调整偏移范围为0至360
    private int sat;//饱和度调整范围为-100至100
    private int bright;//亮度调整范围为-100至100
    private int contrast;//对比度调整范围为-100至100

    private boolean isAuto;

    public ConfigBean() {
    }

    public ConfigBean(int seconds, int hue, int sat, int bright, int contrast, boolean isAuto) {
        this.seconds = seconds;
        this.hue = hue;
        this.sat = sat;
        this.bright = bright;
        this.contrast = contrast;
        this.isAuto = isAuto;
    }


    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getBright() {
        return bright;
    }

    public void setBright(int bright) {
        this.bright = bright;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }
}
