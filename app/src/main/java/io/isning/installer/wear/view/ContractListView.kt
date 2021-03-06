package io.isning.installer.wear.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.isning.installer.wear.R
import io.isning.installer.wear.adapter.ActivityAdapter
import io.isning.installer.wear.adapter.PermissionAdapter
import io.isning.installer.wear.data.PermFullEntity
import io.isning.installer.wear.databinding.LayoutContractListBinding
import io.isning.installer.wear.utils.lazyBind
import io.isning.installer.wear.utils.visibleOrGone
import java.util.ArrayList

class ContractListView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private val rootView: LayoutContractListBinding by lazyBind()

    private var rvData: RecyclerView = rootView.rvContract

    private fun initView() {
        rvData.layoutManager = LinearLayoutManager(context)
        rootView.csContract.setOnClickListener {
            startViewCloseUpFun()
        }
        addView(rootView.root)
    }

    fun setTitle(title: String) {
        rootView.tvContract.text = title
    }

    fun setScrollView(view: View) {
        rootView.rvContract.addOnScrollListener(RvScrollListener(view))
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("NotifyDataSetChanged")
    fun setListData(data: ArrayList<*>) = if (data[0] is PermFullEntity) {
        rvData.adapter = PermissionAdapter(data as ArrayList<PermFullEntity>).apply {
            setItemClickListener {
                permInfoDialog(it)
            }
            notifyDataSetChanged()
        }
    } else {
        rvData.adapter = ActivityAdapter(data as ArrayList<String>).apply {
            notifyDataSetChanged()
        }
    }

    private fun permInfoDialog(entity: PermFullEntity) {
        //val group = if (entity.des.isEmpty()) resources.getString(R.string.text_no_description) else entity.group
        val lab = if (entity.lab.isEmpty()) resources.getString(R.string.text_no_description) else entity.lab
        val des = if (entity.des.isEmpty()) resources.getString(R.string.text_no_description) else entity.des

        CustomizeDialog.getInstance(context)
                //.setTitle(group)
                .setMessage(entity.perm + "\n\n" + lab + "\n\n" + des)
                .setPositiveButton(R.string.dialog_btn_ok, null)
                .create()
                .show()
    }

    private fun startViewCloseUpFun() {
        rootView.rvContract.visibility.let { vis ->
            (vis == View.GONE).let {
                if (it) {
                    rootView.ivContractArrow.setImageResource(R.drawable.ic_expand)
                } else {
                    rootView.ivContractArrow.setImageResource(R.drawable.ic_right)
                }
                rootView.rvContract.visibleOrGone(it)
            }
        }
    }

}
