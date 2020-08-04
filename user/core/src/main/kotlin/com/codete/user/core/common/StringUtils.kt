package com.codete.user.core.common

import org.apache.commons.validator.routines.EmailValidator

object StringUtils {
    fun isEmpty(string: String?) = string == null || string.trim().isEmpty()

    fun isValidEmail(string: String?) = EmailValidator.getInstance().isValid(string)}