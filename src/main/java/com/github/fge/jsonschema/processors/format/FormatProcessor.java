/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.processors.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.keyword.validator.KeywordValidator;
import com.github.fge.jsonschema.library.Dictionary;
import com.github.fge.jsonschema.processing.Processor;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.jsonschema.processors.data.SchemaContext;
import com.github.fge.jsonschema.processors.data.ValidatorList;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.NodeType;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import static com.github.fge.jsonschema.messages.FormatMessages.*;

public final class FormatProcessor
    implements Processor<ValidatorList, ValidatorList>
{
    private final Map<String, FormatAttribute> attributes;

    public FormatProcessor(final Dictionary<FormatAttribute> dict)
    {
        attributes = dict.entries();
    }

    @Override
    public ValidatorList process(final ProcessingReport report,
        final ValidatorList input)
        throws ProcessingException
    {
        final SchemaContext context = input.getContext();
        final JsonNode node = context.getSchema().getNode().get("format");

        if (node == null)
            return input;

        final String fmt = node.textValue();
        final FormatAttribute attr = attributes.get(fmt);

        if (attr == null) {
            report.warn(input.newMessage().put("domain", "validation")
                .put("keyword", "format").message(FORMAT_NOT_SUPPORTED)
                .put("attribute", fmt));
            return input;
        }

        final NodeType type = context.getInstanceType();

        if (!attr.supportedTypes().contains(type))
            return input;

        final List<KeywordValidator> validators = Lists.newArrayList(input);
        validators.add(formatValidator(attr));

        return new ValidatorList(context, validators);
    }

    private static KeywordValidator formatValidator(final FormatAttribute attr)
    {
        return new KeywordValidator()
        {
            @Override
            public void validate(
                final Processor<FullData, FullData> processor,
                final ProcessingReport report, final FullData data)
                throws ProcessingException
            {
                attr.validate(report, data);
            }
        };
    }

    @Override
    public String toString()
    {
        return "format attribute runner";
    }
}
