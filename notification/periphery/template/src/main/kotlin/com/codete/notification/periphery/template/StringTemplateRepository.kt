package com.codete.notification.periphery.template

import com.codete.notification.core.repository.TemplateRepository
import org.stringtemplate.v4.ST

internal class StringTemplateRepository(
        private val templates: Map<String, String>
) : TemplateRepository {
    override fun create(name: String, context: Map<String, String>): String {
        val template = ST(templates.getOrDefault(name, name))
        context.forEach { (key, value) ->
            template.add(key, value)
        }
        return template.render()
    }
}