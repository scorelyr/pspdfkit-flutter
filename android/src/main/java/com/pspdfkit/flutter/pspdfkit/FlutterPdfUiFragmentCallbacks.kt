package com.pspdfkit.flutter.pspdfkit

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.pspdfkit.annotations.AnnotationProvider
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.flutter.pspdfkit.util.MeasurementHelper
import com.pspdfkit.listeners.SimpleDocumentListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import io.flutter.plugin.common.MethodChannel

class FlutterPdfUiFragmentCallbacks(
    val methodChannel: MethodChannel,
    val measurementConfigurations: List<Map<String, Any>>?
) : FragmentManager.FragmentLifecycleCallbacks() {

    var pdfFragment: PdfFragment? = null

    override fun onFragmentAttached(
        fm: FragmentManager,
        f: Fragment,
        context: Context
    ) {
        if (f.tag?.contains("PSPDFKit.Fragment") == true) {
            EventDispatcher.getInstance().notifyPdfFragmentAdded()
            if (f !is PdfFragment) {
                return
            }
            if (pdfFragment != null) {
                return
            }
            pdfFragment = f as PdfFragment
            pdfFragment?.addDocumentListener(object : SimpleDocumentListener() {
                override fun onDocumentLoaded(document: PdfDocument) {
                    measurementConfigurations?.forEach {
                        MeasurementHelper.addMeasurementConfiguration(pdfFragment!!, it)
                    }
                    methodChannel.invokeMethod(
                        "onDocumentLoaded", mapOf(
                            "documentId" to document.uid
                        )
                    )
                }

                override fun onPageChanged(document: PdfDocument, pageIndex: Int) {
                    super.onPageChanged(document, pageIndex)
                    methodChannel.invokeMethod(
                        "onPageChanged",
                        mapOf(
                            "documentId" to document.uid,
                            "pageIndex" to pageIndex
                        )
                    )
                }

                override fun onDocumentLoadFailed(exception: Throwable) {
                    super.onDocumentLoadFailed(exception)
                    methodChannel.invokeMethod(
                        "onDocumentLoadFailed",
                        mapOf(
                            "error" to exception.message
                        )
                    )
                }
            })

            pdfFragment?.addOnAnnotationCreationModeChangeListener(object :
                AnnotationManager.OnAnnotationCreationModeChangeListener {
                override fun onEnterAnnotationCreationMode(p0: AnnotationCreationController) {

                }

                override fun onChangeAnnotationCreationMode(p0: AnnotationCreationController) {

                }

                override fun onExitAnnotationCreationMode(annotationCreationController: AnnotationCreationController) {
                    methodChannel.invokeMethod("onExitAnnotationCreationMode", null)
                }

            })

            pdfFragment?.addOnAnnotationUpdatedListener(object :
                AnnotationProvider.OnAnnotationUpdatedListener {
                override fun onAnnotationCreated(annotation: Annotation) {
                    methodChannel.invokeMethod(
                        "onAnnotationCreated",
                        mapOf(
                            "annotationJsonString" to annotation.toInstantJson(),
                        )
                    )
                }

                override fun onAnnotationUpdated(annotation: Annotation) {
                    methodChannel.invokeMethod(
                        "onAnnotationUpdated",
                        mapOf(
                            "annotationJsonString" to annotation.toInstantJson(),
                        )
                    )
                }

                override fun onAnnotationRemoved(annotation: Annotation) {
                    methodChannel.invokeMethod(
                        "onAnnotationRemoved",
                        mapOf(
                            "annotationUuid" to annotation.uuid,
                        )
                    )
                }

                override fun onAnnotationZOrderChanged(
                    p0: Int,
                    p1: MutableList<Annotation>,
                    p2: MutableList<Annotation>
                ) {
                    // Not implemented
                }
            })
        }
    }
}