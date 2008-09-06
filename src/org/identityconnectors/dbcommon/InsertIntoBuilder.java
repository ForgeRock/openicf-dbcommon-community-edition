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


import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.CollectionUtil;


/**
 * The update set builder create the database update statement.
 * <p>The main functionality is create set part of update statement from Attribute set</p>
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class InsertIntoBuilder {
    private List<Object> params = new ArrayList<Object>();
    private StringBuilder into = new StringBuilder();
    private StringBuilder values = new StringBuilder();
    private boolean first = true;
       
    /**
     * Add column name and value pair
     * 
     * @param name name
     * @param param parameter
     * @return self, builder pattern
     */
    public InsertIntoBuilder addBind(String name, Object param) {
        if(!first) {
            into.append(", ");
            values.append(", ");
        }
        into.append(name);
        values.append("?");
        params.add(param);
        first = false;
        return this;
    }
    
    /**
     * Build the into 
     * @return The SQL part 
     */
    public String getInto() {
        return into.toString();
    }

    /**
     * Build the values 
     * @return The SQL part 
     */
    public String getValues() {
        return values.toString();
    }

    /**
     * @return the values
     */
    public List<Object> getParams() {
        return CollectionUtil.asReadOnlyList(params);
    }
}