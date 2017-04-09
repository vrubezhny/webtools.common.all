package org.eclipse.wst.common.project.facet.core.tests.support;

import org.eclipse.wst.common.project.facet.core.DefaultVersionComparator;
import org.eclipse.wst.common.project.facet.core.VersionFormatException;

public final class CustomVersionComparator

    extends DefaultVersionComparator
    
{
    protected String getSeparators()
    {
        return ".#";
    }
    
    protected Comparable parse( final String version,
                                final String segment,
                                final int position )
    
        throws VersionFormatException
        
    {
        if( position == 2 )
        {
            return new Inverter( segment );
        }
        else
        {
            return super.parse( version, segment, position );
        }
    }
    
    public static class Inverter
    
        implements Comparable
        
    {
        private final Comparable base;
        
        public Inverter( final Comparable base )
        {
            this.base = base;
        }
        
        public boolean equals( final Object obj )
        {
            if( ! ( obj instanceof Inverter ) )
            {
                return false;
            }
            else
            {
                return this.base.equals( ( (Inverter) obj ).base );
            }
        }
        
        public int compareTo( final Object obj )
        {
            return -1 * this.base.compareTo( ( (Inverter) obj ).base );
        }
    }

}
