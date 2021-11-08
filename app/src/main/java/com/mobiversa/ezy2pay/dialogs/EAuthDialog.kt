package com.mobiversa.ezy2pay.dialogs

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.network.response.ProductList

class EAuthDialog(
    val callback: EAuthDialogInterface,
    private val productList: ArrayList<ProductList>,
    private val auth3dEnabled: String
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_e_auth_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<TextView>(R.id.textView_ezy_wire).apply {
            text = if (productList[1].isEnable) {
                requireContext().getText(R.string.ezywireEnabled)
            } else {
                requireContext().getText(R.string.ezywireDisabled)
            }
            isEnabled = productList[1].isEnable

            if (!productList[0].isEnable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextColor(
                        requireContext().resources.getColor(
                            android.R.color.darker_gray,
                            requireContext().theme
                        )
                    )
                } else {
                    setTextColor(requireContext().resources.getColor(android.R.color.darker_gray))
                }

            setOnClickListener {
                callback.onEzyWire()
                dismiss()
            }
        }
        view.findViewById<TextView>(R.id.textView_digital).apply {
            text = if (auth3dEnabled.equals("YES", ignoreCase = true)) {
                if (productList[0].isEnable)
                    requireContext().getText(R.string.ezyLinkEnabled)
                else
                    requireContext().getText(R.string.ezyLinkDisabled)
            } else {
                if (productList[0].isEnable)
                    requireContext().getText(R.string.digitalEnabled)
                else
                    requireContext().getText(R.string.digitalDisabled)
            }
            isEnabled = productList[0].isEnable

            if (!productList[0].isEnable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextColor(
                        requireContext().resources.getColor(
                            android.R.color.darker_gray,
                            requireContext().theme
                        )
                    )
                } else {
                    setTextColor(requireContext().resources.getColor(android.R.color.darker_gray))
                }

            setOnClickListener {
                callback.onDigital()
                dismiss()
            }
        }

        view.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            callback.onCancel()
            dismiss()
        }
    }

    companion object {
        fun newInstance(
            callback: EAuthDialogInterface,
            productList: ArrayList<ProductList>,
            auth3dEnabled: String
        ) = EAuthDialog(callback, productList, auth3dEnabled)
    }


    interface EAuthDialogInterface {
        fun onEzyWire()
        fun onDigital()
        fun onCancel()
    }
}