package org.eclipse.wst.common.project.facet.core.runtime.classpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.internal.FacetCorePlugin;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public final class ClasspathHelper
{
    private static final Object SYSTEM_OWNER = new Object();
    
    private ClasspathHelper() {}
    
    public static boolean addClasspathEntries( final IProject project,
                                               final IProjectFacetVersion fv )
    
        throws CoreException
        
    {
        final IFacetedProject fproj 
            = ProjectFacetsManager.create( project );
    
        final IRuntime runtime = fproj.getRuntime();
        
        if( runtime != null )
        {
            final IClasspathProvider cpprov 
                = (IClasspathProvider) runtime.getAdapter( IClasspathProvider.class );
            
            final List cpentries = cpprov.getClasspathEntries( fv );
            
            if( cpentries != null )
            {
                addClasspathEntries( project, fv, cpentries );
                return true;
            }
        }
        
        return false;
    }
    
    public static void addClasspathEntries( final IProject project,
                                            final IProjectFacetVersion fv,
                                            final List cpentries )
    
        throws CoreException
        
    {
        try
        {
            final IJavaProject jproj = JavaCore.create( project );
            final List cp = getClasspath( jproj );
            boolean cpchanged = false;

            final Map prefs = readPreferences( project );
            
            for( Iterator itr = cpentries.iterator(); itr.hasNext(); )
            {
                final IClasspathEntry cpentry = (IClasspathEntry) itr.next();
                final IPath path = cpentry.getPath();
                
                final boolean contains = cp.contains( cpentry );
                
                Set owners = (Set) prefs.get( path );
                
                if( owners == null )
                {
                    owners = new HashSet();

                    if( contains )
                    {
                        owners.add( SYSTEM_OWNER );
                    }
                    
                    prefs.put( path, owners );
                }
                
                owners.add( fv );
                
                if( ! contains )
                {
                    cp.add( cpentry );
                    cpchanged = true;
                }
            }
            
            if( cpchanged )
            {
                setClasspath( jproj, cp );
            }
            
            writePreferences( project, prefs );
        }
        catch( BackingStoreException e )
        {
            // TODO: throw CoreException here
            throw new RuntimeException( e );
        }
    }
    
    public static void removeClasspathEntries( final IProject project,
                                               final IProjectFacetVersion fv )
    
        throws CoreException
        
    {
        try
        {
            final IJavaProject jproj = JavaCore.create( project );
            final List cp = getClasspath( jproj );
            boolean cpchanged = false;

            final Map prefs = readPreferences( project );
            
            for( Iterator itr1 = prefs.entrySet().iterator(); itr1.hasNext(); )
            {
                final Map.Entry entry = (Map.Entry) itr1.next();
                final IPath path = (IPath) entry.getKey();
                final Set owners = (Set) entry.getValue();
                
                if( owners.contains( fv ) )
                {
                    owners.remove( fv );
                    
                    if( owners.size() == 0 )
                    {
                        itr1.remove();
                        
                        for( Iterator itr2 = cp.iterator(); itr2.hasNext(); )
                        {
                            final IClasspathEntry cpentry
                                = (IClasspathEntry) itr2.next();
                            
                            if( cpentry.getPath().equals( path ) )
                            {
                                itr2.remove();
                                cpchanged = true;
                                break;
                            }
                        }
                    }
                }
            }

            if( cpchanged )
            {
                setClasspath( jproj, cp );
            }
            
            writePreferences( project, prefs );
        }
        catch( BackingStoreException e )
        {
            // TODO: throw CoreException here
            throw new RuntimeException( e );
        }
    }
    
    private static List getClasspath( final IJavaProject jproj )
    
        throws CoreException
        
    {
        final ArrayList list = new ArrayList();
        final IClasspathEntry[] cp = jproj.getRawClasspath();
        
        for( int i = 0; i < cp.length; i++ )
        {
            list.add( cp[ i ] );
        }
        
        return list;
    }
    
    private static void setClasspath( final IJavaProject jproj,
                                      final List cp )
    
        throws CoreException
        
    {
        final IClasspathEntry[] newcp
            = (IClasspathEntry[]) cp.toArray( new IClasspathEntry[ cp.size() ] );
        
        jproj.setRawClasspath( newcp, null );
    }
    
    private static Map readPreferences( final IProject project )
    
        throws BackingStoreException
        
    {
        final Preferences root = getPreferencesNode( project );
        final Map result = new HashMap();
        
        final String[] keys = root.childrenNames();
        
        for( int i = 0; i < keys.length; i++ )
        {
            final String key = keys[ i ];
            final Preferences node = root.node( key );
            
            final String owners = node.get( "owners", null );
            final String[] split = owners.split( ";" );
            final Set set = new HashSet();
            
            for( int j = 0; j < split.length; j++ )
            {
                final String segment = split[ j ];
                
                if( segment.equals( "#system#" ) )
                {
                    set.add( SYSTEM_OWNER );
                }
                else
                {
                    final IProjectFacetVersion fv 
                        = parseFeatureVersion( segment );
                    
                    set.add( fv );
                }
            }
            
            result.put( decode( key ), set );
        }
        
        return result;
    }
    
    private static void writePreferences( final IProject project,
                                          final Map prefs )
    
        throws BackingStoreException
        
    {
        final Preferences root = getPreferencesNode( project );
        final String[] children = root.childrenNames();
        
        for( int i = 0; i < children.length; i++ )
        {
            root.node( children[ i ] ).removeNode();
        }
        
        for( Iterator itr1 = prefs.entrySet().iterator(); itr1.hasNext(); )
        {
            final Map.Entry entry = (Map.Entry) itr1.next();
            final IPath path = (IPath) entry.getKey();
            final Set owners = (Set) entry.getValue();
            
            final StringBuffer buf = new StringBuffer();
            
            for( Iterator itr2 = owners.iterator(); itr2.hasNext(); )
            {
                final Object owner = itr2.next();

                if( buf.length() > 0 ) 
                {
                    buf.append( ';' );
                }
                
                if( owner == SYSTEM_OWNER )
                {
                    buf.append( "#system#" );
                }
                else
                {
                    final IProjectFacetVersion fv 
                        = (IProjectFacetVersion) owner;
                    
                    buf.append( fv.getProjectFacet().getId() );
                    buf.append( ':' );
                    buf.append( fv.getVersionString() );
                }
            }

            final Preferences node = root.node( encode( path ) );
            node.put( "owners", buf.toString() );
        }
        
        root.flush();
    }
    
    
    private static Preferences getPreferencesNode( final IProject project )
    {
        final ProjectScope scope = new ProjectScope( project );
        
        final IEclipsePreferences pluginRoot 
            = scope.getNode( FacetCorePlugin.PLUGIN_ID );
        
        return pluginRoot.node( "classpath.helper" );
    }
    
    private static IProjectFacetVersion parseFeatureVersion( final String str )
    {
        final int colon = str.indexOf( ':' );
        final String id = str.substring( 0, colon );
        final String ver = str.substring( colon + 1 );
        
        return ProjectFacetsManager.getProjectFacet( id ).getVersion( ver );
    }
    
    private static String encode( final IPath path )
    {
        return path.toString().replaceAll( "/", "::" );
    }
    
    private static IPath decode( final String path )
    {
        return new Path( path.replaceAll( "::", "/" ) );
    }
    

}
