package com.catchebstnew.www.callback;


/**
 * 类功能描述：6.0运行时权限 </br>
 * 接口功能回调 </br>
 * 博客地址：http://blog.csdn.net/androidstarjack
 * @author 老于
 * Created  on 2017/1/3/002
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public interface QuestPermissionListener {
    void  doAllPermissionGrant();
    void  denySomePermission();
}
