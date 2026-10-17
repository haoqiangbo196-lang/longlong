package com.elegant.reader.data

import cn.bmob.v3.BmobObject

/**
 * 求书记录（Bmob 数据表 BookRequest）。
 *
 * 生命周期：
 *  1. 用户提交求书 → status = "pending"（待处理）
 *  2. 开发者在 Bmob 后台看到记录，上传书文件，把文件地址填入 bookFileUrl，
 *     status 改为 "ready"
 *  3. 用户 App 查询到 ready 记录 → 显示下载按钮 → 下载后导入书架
 */
class BookRequest : BmobObject() {

    /** 设备唯一标识（匿名用户标识） */
    var deviceId: String = ""

    /** 想看的书名 */
    var bookTitle: String = ""

    /** 备注（可选） */
    var note: String = ""

    /** 状态：pending = 待处理，ready = 书已提供 */
    var status: String = "pending"

    /** 书文件下载地址（开发者上传后填入） */
    var bookFileUrl: String = ""

    /** 书文件名（含扩展名，用于导入书架） */
    var bookFileName: String = ""
}
