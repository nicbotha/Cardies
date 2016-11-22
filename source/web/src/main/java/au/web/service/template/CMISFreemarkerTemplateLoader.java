package au.web.service.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.web.service.cmis.CMISService;
import freemarker.cache.TemplateLoader;

/**
 * A {@link TemplateLoader} for loading templates from any repository that supports CMIS.
 * 
 * @author Andreas Magnusson Monator Technologies AB
 * 
 */
@Component(value="CMISFreemarkerTemplateLoader")
public class CMISFreemarkerTemplateLoader implements TemplateLoader {

    @Autowired
	protected CMISService cmisService;

    /** Constant to use for logging. */
    private static final Logger log = LoggerFactory.getLogger(CMISFreemarkerTemplateLoader.class);

    /**
     * Creates a new CMIS Freemarker template loader which uses the specified values when loading templates.
     * 
     * @param folderPath
     *            folder path to where you store your templates.
     * @param createFolder
     *            if true, the folders in folderPath will be created when not existing.
     * @param defaultTemplate
     *            default template which will be copied to the created folder when createFolder is true.
     */
    public CMISFreemarkerTemplateLoader() {
    }

    /**
     * Uses Apache OpenCMIS's API through a convenience class, {@link CMISConnection}, to fetch the template from the repository. Folder
     * path is created if missing, depending on the value of <code>create_folder</code>.
     * 
     * {@inheritDoc}
     */
    public final Object findTemplateSource(final String name) throws IOException {
    	log.info(">> findTemplateSource={}", name);
        Object fmTemplate = null;
        
        fmTemplate = cmisService.getObjectById(name);

        return fmTemplate;
    }

    /* (non-Javadoc)
     * @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object)
     */
    public final long getLastModified(final Object templateSource) {

        return ((CmisObject) templateSource).getLastModificationDate().getTimeInMillis();
    }

    /* (non-Javadoc)
     * @see freemarker.cache.TemplateLoader#getReader(java.lang.Object, java.lang.String)
     */
    public final Reader getReader(Object templateSource, final String encoding) throws IOException {

        return new InputStreamReader(((Document) templateSource).getContentStream().getStream(), encoding);
    }

    /* (non-Javadoc)
     * @see freemarker.cache.TemplateLoader#closeTemplateSource(java.lang.Object)
     */
    public final void closeTemplateSource(final Object templateSource) throws IOException {
        // Do nothing
    }
    
   
    

}
