package com.bonlala.action;


import com.bonlala.base.BaseFragment;


/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : Fragment 业务基类
 */
public abstract class AppFragment<A extends AppActivity> extends BaseFragment<A>
        implements ToastAction {

    /**
     * 当前加载对话框是否在显示中
     */
    public boolean isShowDialog() {
        A activity = getAttachActivity();
        if (activity == null) {
            return false;
        }
        return activity.isShowDialog();
    }

    /**
     * 显示加载对话框
     */
    public void showDialog() {
        A activity = getAttachActivity();
        if (activity == null) {
            return;
        }
        activity.showDialog();
    }

    /**
     * 显示加载对话框
     */
    public void showDialog(String txt) {
        A activity = getAttachActivity();
        if (activity == null) {
            return;
        }
        activity.showDialog(txt);
    }


    /**
     * 显示加载对话框
     */
    public void showDialog(String txt,boolean isCancel) {
        A activity = getAttachActivity();
        if (activity == null) {
            return;
        }
        activity.showDialog(txt);
    }

    /**
     * 隐藏加载对话框
     */
    public void hideDialog() {
        A activity = getAttachActivity();
        if (activity == null) {
            return;
        }
        activity.hideDialog();
    }


}