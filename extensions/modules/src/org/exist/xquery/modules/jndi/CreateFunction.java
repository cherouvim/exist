/*
 *  eXist SQL Module Extension GetConnectionFunction
 *  Copyright (C) 2008-09 Adam Retter <adam@exist-db.org>
 *  www.adamretter.co.uk
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */

package org.exist.xquery.modules.jndi;


import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * eXist JNDI Module Extension CreateFunction
 * 
 * Create a JNDI Directory entry
 * 
 * @author Andrzej Taramina <andrzej@chaeron.com>
 * @serial 2008-12-02
 * @version 1.0
 * 
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext,
 *      org.exist.xquery.FunctionSignature)
 */

public class CreateFunction extends BasicFunction 
{
	protected static final Logger logger = LogManager.getLogger(CreateFunction.class);
	
	public final static String DSML_NAMESPACE = "http://www.dsml.org/DSML";

	public final static String DSML_PREFIX = "dsml";

	public final static FunctionSignature[] signatures = {
			
			new FunctionSignature(
					new QName( "create", JNDIModule.NAMESPACE_URI, JNDIModule.PREFIX ),
							"Create a JNDI Directory entry.",
					new SequenceType[] {
						new FunctionParameterSequenceType( "directory-context", Type.INTEGER, Cardinality.EXACTLY_ONE, "The directory context handle from a jndi:get-dir-context() call" ), 
						new FunctionParameterSequenceType( "dn", Type.STRING, Cardinality.EXACTLY_ONE, "The Distinguished Name" ), 
						new FunctionParameterSequenceType( "attributes", Type.ELEMENT, Cardinality.EXACTLY_ONE, "The entry attributes to be set in the"
							+ " form <attributes><attribute name=\"\" value=\"\"/></attributes>. "
							+ " You can also optionally specify ordered=\"true\" for an attribute." ) 
					},
					new SequenceType( Type.ITEM, Cardinality.EMPTY ) )
			};

	/**
	 * CreateFunction Constructor
	 * 
	 * @param context 	The Context of the calling XQuery
	 */
	
	public CreateFunction( XQueryContext context, FunctionSignature signature ) 
	{
		super( context, signature );
	}

	
	/**
	 * evaluate the call to the xquery create() function, it is really
	 * the main entry point of this class
	 * 
	 * @param args				arguments from the get-connection() function call
	 * @param contextSequence 	the Context Sequence to operate on (not used here internally!)
	 * @return 					A xs:long representing a handle to the connection
	 * 
	 * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[],
	 *      org.exist.xquery.value.Sequence)
	 */
	
	public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException 
	{
		// Was context handle or DN specified?
		if( !( args[0].isEmpty() ) && !( args[1].isEmpty() ) ) {
			
			String dn = args[1].getStringValue();
			
			try {
				long ctxID = ((IntegerValue)args[0].itemAt(0)).getLong();
				
				DirContext ctx = (DirContext)JNDIModule.retrieveJNDIContext( context, ctxID );
				
				if( ctx == null ) {
					logger.error( "jndi:create() - Invalid JNDI context handle provided: " + ctxID );
				} else {	
					BasicAttributes attributes = JNDIModule.parseAttributes( args[ 2 ] );
					
					if( attributes.size() > 0 ) {
						ctx.createSubcontext( dn, attributes );
					} else {
						ctx.createSubcontext( dn );
					}
				}
			}
			catch( NamingException ne ) {
				logger.error( "jndi:create() Create failed for dn [" + dn + "]: ", ne );
				throw( new XPathException( this, "jndi:create() Create failed for dn [" + dn + "]: " + ne ) );
			}
		}
		
		return( Sequence.EMPTY_SEQUENCE );
	}

}
