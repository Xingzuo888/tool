package com.tool

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.tool.utils.ToastUtil
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 *    Author : wxz
 *    Time   : 2020/12/15
 *    Desc   :
 */
class PermissionsFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val PERMISSION_STORAGE_MSG = "请授予权限，否则影响部分使用功能"
    private val PERMISSION_STORAGE_CODE = 10001
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    private val options by lazy {
        navOptions {
            popUpTo(R.id.permissions_fragment) {
                inclusive = true
            }
            anim {
                enter = R.anim.fade_in
                exit = R.anim.fade_out
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (EasyPermissions.hasPermissions(context!!, *permissions)) {
            // 已经申请过权限，做想做的事
            findNavController().navigate(R.id.home_fragment, null, options)
        } else {
            // 没有申请过权限，现在去申请
            requestPermissions()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * 申请成功时调用
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.hasPermissions(context!!, *permissions)) {
            findNavController().navigate(R.id.home_fragment, null, options)
        } else if (!EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AlertDialog.Builder(context!!)
                .setMessage(R.string.permission_failed_tip)
                .setNegativeButton(
                    R.string.cancel_btn
                ) { dialog, p1 ->
                    dialog?.dismiss()
                    activity?.finish()
                }
                .setPositiveButton(
                    R.string.ok_btn
                ) { dialog, p1 ->
                    dialog?.dismiss()
                    requestPermissions()
                }
                .create()
                .show()
        }
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            PERMISSION_STORAGE_MSG,
            PERMISSION_STORAGE_CODE,
            *permissions
        )
    }

    /**
     * 申请拒绝时调用
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        ToastUtil.showForce("用户授权失败")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //从设置页面返回，判断权限是否申请。
            if (EasyPermissions.hasPermissions(context!!, *permissions)) {
                ToastUtil.showForce("权限申请成功!")
                findNavController().navigate(R.id.home_fragment, null, options)
            } else {
                ToastUtil.showForce("权限申请失败!")
                activity?.finish()
            }
        }
    }
}