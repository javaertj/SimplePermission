package com.ykbjson.lib.simplepermission;

/**
 * 包名：com.ykbjson.lib.simplepermission
 * 描述：Enum class to handle the different states
 * of permissions since the PackageManager only
 * has a granted and denied state.
 * 创建者：yankebin
 * 日期：2017/5/12
 */
enum Permissions {
  GRANTED,
  DENIED,
  NOT_FOUND,
  USER_DENIED_FOREVER
}