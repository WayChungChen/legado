package io.legado.app.ui.replacerule

import android.app.Application
import android.text.TextUtils
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.ReplaceRule
import io.legado.app.help.http.HttpHelper
import io.legado.app.help.storage.Backup
import io.legado.app.help.storage.Restore
import io.legado.app.utils.FileUtils
import io.legado.app.utils.GSON
import io.legado.app.utils.isAbsUrl
import io.legado.app.utils.splitNotBlank
import org.jetbrains.anko.toast
import java.io.File

class ReplaceRuleViewModel(application: Application) : BaseViewModel(application) {

    fun importSource(text: String, showMsg: (msg: String) -> Unit) {
        execute {
            if (text.isAbsUrl()) {
                HttpHelper.simpleGet(text)?.let {
                    Restore.importOldReplaceRule(it)
                }
            } else {
                Restore.importOldReplaceRule(text)
            }
        }.onError {
            showMsg(it.localizedMessage ?: "ERROR")
        }.onSuccess {
            showMsg(context.getString(R.string.success))
        }
    }

    fun update(vararg rule: ReplaceRule) {
        execute {
            App.db.replaceRuleDao().update(*rule)
        }
    }

    fun delete(rule: ReplaceRule) {
        execute {
            App.db.replaceRuleDao().delete(rule)
        }
    }

    fun toTop(rule: ReplaceRule) {
        execute {
            rule.order = App.db.replaceRuleDao().minOrder - 1
            App.db.replaceRuleDao().update(rule)
        }
    }

    fun upOrder() {
        execute {
            val rules = App.db.replaceRuleDao().all
            for ((index: Int, rule: ReplaceRule) in rules.withIndex()) {
                rule.order = index + 1
            }
            App.db.replaceRuleDao().update(*rules.toTypedArray())
        }
    }

    fun enableSelection(rules: LinkedHashSet<ReplaceRule>) {
        execute {
            val list = ArrayList(rules)
            list.forEach {
                it.isEnabled = true
            }
            App.db.replaceRuleDao().update(*list.toTypedArray())
        }
    }

    fun disableSelection(rules: LinkedHashSet<ReplaceRule>) {
        execute {
            val list = ArrayList(rules)
            list.forEach {
                it.isEnabled = false
            }
            App.db.replaceRuleDao().update(*list.toTypedArray())
        }
    }

    fun delSelection(rules: LinkedHashSet<ReplaceRule>) {
        execute {
            App.db.replaceRuleDao().delete(*rules.toTypedArray())
        }
    }

    fun exportSelection(rules: LinkedHashSet<ReplaceRule>) {
        execute {
            val json = GSON.toJson(rules)
            val file =
                FileUtils.createFileIfNotExist(Backup.exportPath + File.separator + "exportReplaceRule.json")
            file.writeText(json)
        }.onSuccess {
            context.toast("成功导出至\n${Backup.exportPath}")
        }.onError {
            context.toast("导出失败\n${it.localizedMessage}")
        }
    }

    fun addGroup(group: String) {
        execute {
            val sources = App.db.replaceRuleDao().noGroup
            sources.map { source ->
                source.group = group
            }
            App.db.replaceRuleDao().update(*sources.toTypedArray())
        }
    }

    fun upGroup(oldGroup: String, newGroup: String?) {
        execute {
            val sources = App.db.replaceRuleDao().getByGroup(oldGroup)
            sources.map { source ->
                source.group?.splitNotBlank(",")?.toHashSet()?.let {
                    it.remove(oldGroup)
                    if (!newGroup.isNullOrEmpty())
                        it.add(newGroup)
                    source.group = TextUtils.join(",", it)
                }
            }
            App.db.replaceRuleDao().update(*sources.toTypedArray())
        }
    }

    fun delGroup(group: String) {
        execute {
            execute {
                val sources = App.db.replaceRuleDao().getByGroup(group)
                sources.map { source ->
                    source.group?.splitNotBlank(",")?.toHashSet()?.let {
                        it.remove(group)
                        source.group = TextUtils.join(",", it)
                    }
                }
                App.db.replaceRuleDao().update(*sources.toTypedArray())
            }
        }
    }
}
