package com.mozhimen.camerak.camerax.test

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import com.mozhimen.bindk.bases.viewdatabinding.activity.BaseActivityVDB
import com.mozhimen.kotlin.lintk.optins.permission.OPermission_CAMERA
import com.mozhimen.camerak.*
import com.mozhimen.camerak_uvc.Direction
import com.mozhimen.camerak.camerax.test.databinding.ActivityCamerakBinding
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil

/**
 * @ClassName CameraKActivity
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/2/21 21:54
 * @Version 1.0
 */
class CameraKUVCActivity : BaseActivityVDB<ActivityCamerakBinding>() {
    @OptIn(OPermission_CAMERA::class)
    @SuppressLint("MissingPermission")
    override fun initData(savedInstanceState: Bundle?) {
        XXPermissionsRequestUtil.requestCameraPermission(this, {
            super.initData(savedInstanceState)
        }, {})
    }

    override fun initView(savedInstanceState: Bundle?) {
        initCamera()
    }

    private lateinit var _surfaceRbg: SurfaceTexture
    private var _cameraMgrRgb: CameraManager? = null

    @Transient
    private var _bytesRgb: ByteArray? = null
    private fun initCamera() {
        vdb.camerakScale.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, p1: Int, p2: Int) {
                _surfaceRbg = surface
                openRgbCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
        vdb.camerakScale.apply {
            setDisplayDir(getDegree(90))
            resetPreviewSize(1280, 800)
            setMirror(false)
        }
    }

    /**
     * 获取方向
     * @param degree 0 90 180 270
     * @return
     */
    private fun getDegree(degree: Int): Direction {
        return when (degree) {
            0 -> Direction.UP
            90 -> Direction.LEFT
            180 -> Direction.DOWN
            270 -> Direction.RIGHT
            else -> Direction.AUTO
        }
    }

    private fun openRgbCamera() {
        _cameraMgrRgb = CameraManager.getInstance(
            CameraFacing.Builder()
//                .setFacingType(FacingType.OTHER)
//                .setCameraId(/*DEFAULT_RGB_CAMERA_FACING*/0)
                .setFacingType(FacingType.FRONT)
                .build(),
            CameraApiType.CAMERA1,
            baseContext,
            Handler(Looper.getMainLooper())
        )
        _cameraMgrRgb!!.setCallBackEvents(object : CallBackEvents {
            override fun onCameraOpen(p0: IAttributes?) {
                val width = /*CameraConfig.preview_width*/1280
                val height = /*CameraConfig.preview_height*/720
                _cameraMgrRgb!!.apply {
                    setPhotoSize(CameraSize(width, height))
                    setPreviewSize(CameraSize(width, height))
//                    if (FaceConfig.face_ori == ImageOrientation.UP) {
                    setPreviewOrientation(Direction.UP.value * 90)
//                    } else {
//                        setPreviewOrientation(Direction.LEFT.value * 90)
//                    }
                    setFocusMode(CameraFocus.CONTINUOUS_VIDEO)
                    clearPreviewCallbackWithBuffer()
                    addPreviewCallbackWithBuffer {
                        _bytesRgb = it
                        //回调代码
                    }
                    startPreview(_surfaceRbg)
                }
            }

            override fun onCameraClose() {}
            override fun onCameraError(p0: String?) {}
            override fun onPreviewStarted() {}
            override fun onPreviewStopped() {}
            override fun onPreviewError(p0: String?) {}
        })
        _cameraMgrRgb!!.openCamera()
    }

}