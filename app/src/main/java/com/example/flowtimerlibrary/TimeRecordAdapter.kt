package com.example.flowtimerlibrary

import androidx.recyclerview.widget.RecyclerView
import com.example.baseadapterslibrary.adapter.normal.BaseRvAdapter
import com.example.baseadapterslibrary.adapter.normal.Inflate
import com.example.baseadapterslibrary.view_holder.BaseViewBindHolder
import com.example.flowtimerlibrary.databinding.ItemTimeRecordBinding

class TimeRecordAdapter : BaseRvAdapter<ItemTimeRecordBinding, String>() {
    override fun bind(binding: ItemTimeRecordBinding, item: String, bindingAdapterPosition: Int, viewHolder: BaseViewBindHolder) {
        binding.txtTime.text = item
    }

    override fun createHolder(binding: ItemTimeRecordBinding, viewHolder: RecyclerView.ViewHolder) {

    }

    override fun getViewBindingInflate(viewType: Int): Inflate<ItemTimeRecordBinding> {
        return ItemTimeRecordBinding::inflate
    }

    override fun partBind(payload: Any, binding: ItemTimeRecordBinding, item: String, bindingAdapterPosition: Int, viewHolder: BaseViewBindHolder) {

    }
}