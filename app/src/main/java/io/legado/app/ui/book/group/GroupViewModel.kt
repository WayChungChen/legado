package io.legado.app.ui.book.group

import android.app.Application
import io.legado.app.App
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.BookGroup

class GroupViewModel(application: Application) : BaseViewModel(application) {

    fun addGroup(groupName: String) {
        execute {
            var id = 1
            val idsSum = App.db.bookGroupDao().idsSum
            while (id and idsSum != 0) {
                id = id.shl(1)
            }
            val bookGroup = BookGroup(
                groupId = id,
                groupName = groupName,
                order = App.db.bookGroupDao().maxOrder.plus(1)
            )
            App.db.bookGroupDao().insert(bookGroup)
        }
    }

    fun upGroup(vararg bookGroup: BookGroup) {
        execute {
            App.db.bookGroupDao().update(*bookGroup)
        }
    }

    fun delGroup(vararg bookGroup: BookGroup) {
        execute {
            App.db.bookGroupDao().delete(*bookGroup)
        }
    }


}