/*
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * U.S. Government Rights - Commercial software. Government users 
 * are subject to the Sun Microsystems, Inc. standard license agreement
 * and applicable provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and Project Identity 
 * Connectors are trademarks or registered trademarks of Sun 
 * Microsystems, Inc. or its subsidiaries in the U.S. and other
 * countries.
 * 
 * UNIX is a registered trademark in the U.S. and other countries,
 * exclusively licensed through X/Open Company, Ltd. 
 * 
 * -----------
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License(CDDL) (the License).  You may not use this file
 * except in  compliance with the License. 
 * 
 * You can obtain a copy of the License at
 * http://identityconnectors.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * -----------
 */
package org.identityconnectors.dbcommon;

import static org.identityconnectors.framework.common.objects.AttributeBuilder.build;
import static org.identityconnectors.framework.common.objects.filter.FilterBuilder.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.junit.Test;


/**
 * Attempts to test the Connector with the framework.
 * TODO test the mapped values
 */
public class DatabaseFilterTranslatorTests {

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testUnaryFilters() throws Exception {
        Attribute attr = build("count", 2);
        Filter filters[] = new Filter[] { equalTo(attr), greaterThan(attr), greaterThanOrEqualTo(attr), lessThan(attr),
                lessThanOrEqualTo(attr) };
        String ops[] = new String[] { "=", ">", ">=", "<", "<=" };
        List<Object> expected = new ArrayList<Object>();
        expected.add(2);

        for (int i = 0; i < filters.length; i++) {
            DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
            List<FilterWhereBuilder> blist = tr.translate(filters[i]);
            assertEquals(1, blist.size());
            final FilterWhereBuilder b = blist.get(0);
            assertEquals("count " + ops[i] + " ?", b.getWhereClause());
            assertEquals(expected, b.getParams());
        }
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testCompositeFilters() throws Exception {
        Filter lf = greaterThan(build("count", 4));
        Filter rf = lessThan(build("count", 20));
        List<Object> expected = new ArrayList<Object>();
        expected.add(4);
        expected.add(20);

        // test and
        Filter f = FilterBuilder.and(lf, rf);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();        
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        FilterWhereBuilder b = blist.get(0);
        assertEquals("count > ? AND count < ?", b.getWhereClause());
        // test or
        assertEquals(expected, b.getParams());
        f = FilterBuilder.or(lf, rf);
        DatabaseFilterTranslator tr2 = getDatabaseFilterTranslator();     
        blist = tr2.translate(f);
        assertEquals(1, blist.size());
        b = blist.get(0);
        assertEquals("count > ? OR count < ?", b.getWhereClause());
        assertEquals(expected, b.getParams());
        // test xor
        // assertEquals(expected, actual);
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testCompositeFilterChainNotOr() throws Exception {
        Filter lf = greaterThan(build("count", 4));
        Filter rf = lessThan(build("count", 20));
        List<Object> expected = new ArrayList<Object>();
        expected.add(4);
        expected.add(20);

        // test and
        Filter f = FilterBuilder.or(lf, rf);
        Filter not = FilterBuilder.not(f);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();     
        List<FilterWhereBuilder> blist = tr.translate(not);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("count <= ? AND count >= ?", b.getWhereClause());
        assertEquals(expected, b.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testCompositeFilterChainOrAnd() throws Exception {
        Filter f1 = greaterThan(build("count", 4));
        Filter f2 = lessThan(build("count", 20));
        Filter f3 = equalTo(build("count", 10));
        List<Object> expected = new ArrayList<Object>();
        expected.add(4);
        expected.add(20);
        expected.add(10);

        // test and
        Filter f12 = FilterBuilder.or(f1, f2);
        Filter f = FilterBuilder.and(f12, f3);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();       
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("( count > ? OR count < ? ) AND count = ?", b.getWhereClause());
        assertEquals(expected, b.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testCompositeFilterChainAndOrAndOrAnd() throws Exception {
        Filter f1 = equalTo(build("a", 1));
        Filter f2 = equalTo(build("b", 1));
        Filter f3 = equalTo(build("c", 1));
        Filter f4 = equalTo(build("d", 1));
        Filter f5 = equalTo(build("e", 1));
        Filter f6 = equalTo(build("f", 1));
        List<Object> expected = new ArrayList<Object>();
        expected.add(1);
        expected.add(1);
        expected.add(1);
        expected.add(1);
        expected.add(1);
        expected.add(1);

        // test and
        Filter f12 = FilterBuilder.or(f1, f2);
        Filter f34 = FilterBuilder.and(f3, f4);
        Filter f56 = FilterBuilder.or(f5, f6);
        Filter f1234 = FilterBuilder.and(f12, f34);
        Filter f = FilterBuilder.or(f1234, f56);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();    
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals(
                "( ( a = ? OR b = ? ) AND ( c = ? AND d = ? ) ) OR ( e = ? OR f = ? )", b
                        .getWhereClause());
        assertEquals(expected, b.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testCompositeFilterChainOrAndNot() throws Exception {
        Filter f1 = greaterThan(build("count", 4));
        Filter f2 = lessThan(build("count", 20));
        Filter f3 = equalTo(build("count", 10));
        List<Object> expected = new ArrayList<Object>();
        expected.add(4);
        expected.add(20);
        expected.add(10);

        // test and
        Filter f1o2 = FilterBuilder.or(f1, f2);
        Filter fn3 = FilterBuilder.not(f3);
        Filter f = FilterBuilder.and(f1o2, fn3);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();      
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("( count > ? OR count < ? ) AND NOT count = ?", b.getWhereClause());
        assertEquals(expected, b.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     * @throws Exception
     */
    @Test
    public void testNotfilter() throws Exception {
        Filter gt = greaterThan(build("count", 4));
        Filter f = FilterBuilder.not(gt);

        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();  
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("count <= ?", b.getWhereClause());
        List<Object> expected = new ArrayList<Object>();
        expected.add(4);
        assertEquals(expected, b.getParams());
    }
    
    DatabaseFilterTranslator getDatabaseFilterTranslator() {
        return new DatabaseFilterTranslator(ObjectClass.ACCOUNT, null) {
            @Override
            protected String getDatabaseColumnName(Attribute attribute, ObjectClass oclass, OperationOptions options) {
                return attribute.getName();
            }

        };
    }
}
