/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.faces.context.FacesContext;

import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.util.ZanataMessages;
import org.zanata.util.StringUtil;
import com.google.common.base.Function;

import lombok.Getter;
import lombok.Setter;

import static org.zanata.rest.dto.stats.TranslationStatistics.StatUnit.WORD;

@Name("projectIterationFilesAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class ProjectIterationFilesAction
{

   private String projectSlug;
   
   private String iterationSlug;

   private String localeId;

   @In
   private ZanataIdentity identity;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   private DocumentDAO documentDAO;
   
   @In
   private LocaleDAO localeDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private PersonDAO personDAO;

   @In
   private TranslationFileService translationFileServiceImpl;

   @In
   private TranslationService translationServiceImpl;

   @In
   private DocumentService documentServiceImpl;

   @In
   private StatisticsResource statisticsServiceImpl;

   @In
   private ZanataMessages zanataMessages;

   private List<HDocument> iterationDocuments;
   
   private String documentNameFilter;

   private TranslationFileUploadHelper translationFileUpload;

   private DocumentFileUploadHelper documentFileUpload;
   
   private HProjectIteration projectIteration;
   
   public void initialize()
   {
      this.iterationDocuments = this.documentDAO.getAllByProjectIteration(this.projectSlug, this.iterationSlug);
      this.translationFileUpload = new TranslationFileUploadHelper();
      this.documentFileUpload = new DocumentFileUploadHelper();
   }
   
   public HLocale getLocale()
   {
      return localeDAO.findByLocaleId(new LocaleId(localeId));
   }

   public boolean filterDocumentByName( Object docObject )
   {
      final HDocument document = (HDocument)docObject;
      
      if( this.documentNameFilter != null && this.documentNameFilter.length() > 0 )
      {
         return document.getName().toLowerCase().contains( this.documentNameFilter.toLowerCase() );
      }
      else
      {
         return true;
      }
   }
   
   @CachedMethodResult(ScopeType.PAGE)
   public TranslationStatistics getTransUnitWordsForDocument(HDocument doc)
   {
      ContainerTranslationStatistics docStatistics =
         this.statisticsServiceImpl.getStatistics(this.projectSlug, this.iterationSlug, doc.getDocId(), true, new String[]{this.localeId});
      TranslationStatistics stats = docStatistics.getStats(this.localeId, WORD);
      return stats;
   }

   @Restrict("#{projectIterationFilesAction.fileUploadAllowed}")
   public String uploadTranslationFile()
   {
      TranslationsResource transRes = null;
      try
      {
         // process the file
         transRes = this.translationFileServiceImpl.parseTranslationFile(this.translationFileUpload.getFileContents(),
               this.translationFileUpload.getFileName());

         // translate it
         List<String> warnings =
            this.translationServiceImpl.translateAllInDoc(this.projectSlug, this.iterationSlug, this.translationFileUpload.getDocId(),
               new LocaleId(this.localeId), transRes, new StringSet(ExtensionType.GetText.toString()),
               this.translationFileUpload.getMergeTranslations() ? MergeType.AUTO : MergeType.IMPORT);

         StringBuilder facesInfoMssg = new StringBuilder("File {0} uploaded.");
         if (!warnings.isEmpty())
         {
            facesInfoMssg.append(" There were some warnings, see below.");
         }

         FacesMessages.instance().add(Severity.INFO, facesInfoMssg.toString(), this.translationFileUpload.getFileName());
         for (String warning : warnings)
         {
            FacesMessages.instance().add(Severity.WARN, warning);
         }
      }
      catch (ZanataServiceException zex)
      {
         FacesMessages.instance().add(Severity.ERROR, zex.getMessage(), this.translationFileUpload.getFileName());
      }

      // NB This needs to be done as for some reason seam is losing the parameters when redirecting
      // This is efectively the same as returning void
      return FacesContext.getCurrentInstance().getViewRoot().getViewId();
   }

   @Restrict("#{projectIterationFilesAction.documentUploadAllowed}")
   public String uploadDocumentFile()
   {
      try
      {
         Resource doc = this.translationFileServiceImpl.parseDocumentFile(this.documentFileUpload.getFileContents(),
              this.documentFileUpload.getDocumentPath(), this.documentFileUpload.getFileName());

         doc.setLang( new LocaleId(this.documentFileUpload.getSourceLang()) );

         // TODO Copy Trans values
         // Extensions are hard-coded to GetText, since it is the only supported format at the time
         this.documentServiceImpl.saveDocument(this.projectSlug, this.iterationSlug,
               doc, new StringSet(ExtensionType.GetText.toString()),
               false);

         FacesMessages.instance().add(Severity.INFO, "Document file {0} uploaded.", this.documentFileUpload.getFileName());
      }
      catch (ZanataServiceException zex)
      {
         FacesMessages.instance().add(Severity.ERROR, zex.getMessage(), this.documentFileUpload.getFileName());
      }
      catch (InvalidStateException isex)
      {
         FacesMessages.instance().add(Severity.ERROR, "Invalid arguments");
      }

      // NB This needs to be done as for some reason seam is losing the parameters when redirecting
      // This is efectively the same as returning void
      return FacesContext.getCurrentInstance().getViewRoot().getViewId();
   }

   public List<HLocale> getAvailableSourceLocales()
   {
      return localeDAO.findAllActive();
   }

   public boolean isFileUploadAllowed()
   {
      HProjectIteration projectIteration = this.projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HLocale hLocale = this.localeDAO.findByLocaleId(new LocaleId(localeId));

      return projectIteration.getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("modify-translation", projectIteration, hLocale);
   }

   public boolean isDocumentUploadAllowed()
   {
      HProjectIteration projectIteration = this.projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      return projectIteration.getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   public List<HDocument> getIterationDocuments()
   {
      return iterationDocuments;
   }

   public void setIterationDocuments(List<HDocument> iterationDocuments)
   {
      this.iterationDocuments = iterationDocuments;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public String getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(String localeId)
   {
      this.localeId = localeId;
   }

   public String getDocumentNameFilter()
   {
      return documentNameFilter;
   }

   public void setDocumentNameFilter(String documentNameFilter)
   {
      this.documentNameFilter = documentNameFilter;
   }

   public TranslationFileUploadHelper getTranslationFileUpload()
   {
      return translationFileUpload;
   }

   public DocumentFileUploadHelper getDocumentFileUpload()
   {
      return documentFileUpload;
   }

   public HProjectIteration getProjectIteration()
   {
      if (this.projectIteration == null)
      {
         this.projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      }
      return this.projectIteration;
   }

   public boolean isUserAllowedToTranslate()
   {
      return !isIterationReadOnly() && !isIterationObsolete() && identity.hasPermission("add-translation", getProjectIteration().getProject(), getLocale());
   }

   public boolean isIterationReadOnly()
   {
      return getProjectIteration().getProject().getStatus() == EntityStatus.READONLY || getProjectIteration().getStatus() == EntityStatus.READONLY;
   }

   public boolean isIterationObsolete()
   {
      return getProjectIteration().getProject().getStatus() == EntityStatus.OBSOLETE || getProjectIteration().getStatus() == EntityStatus.OBSOLETE;
   }

   /**
    * Returns the display zanataMessages to show when a user cannot translate.
    */
   public List<String> getTranslationDeniedReasonMessages()
   {
      List<String> displayMessages = new ArrayList<String>(5);
      final String separator = "<br/>";

      // Account not logged in
      if( identity == null )
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.NotLoggedIn"));
         return displayMessages;
      }

      // Iteration is read only
      if( isIterationReadOnly() )
      {
         displayMessages.add( zanataMessages.getMessage("jsf.iteration.files.translateDenied.VersionIsReadOnly") );
      }

      // Iteration is Obsolete
      if( isIterationObsolete() )
      {
         displayMessages.add( zanataMessages.getMessage("jsf.iteration.files.translateDenied.VersionIsObsolete") );
      }

      // User not member of language team
      if( !personDAO.isMemberOfLanguageTeam( authenticatedAccount.getPerson(), getLocale() ) )
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.UserNotInLanguageTeam", getLocale().retrieveDisplayName()));
      }

      // User not part of the allowed roles
      HIterationProject project = getProjectIteration().getProject();
      if(!SecurityFunctions.isUserAllowedAccess(project))
      {
         //jsf.iteration.files.translateDenied.UserNotInProjectRole
         displayMessages.add( zanataMessages.getMessage("jsf.iteration.files.translateDenied.UserNotInProjectRole",
               StringUtil.concat(project.getAllowedRoles(), ',', new Function<HAccountRole, String>()
               {
                  @Override
                  public String apply(@Nullable HAccountRole from)
                  {
                     return from.getName();
                  }
               })
         ));
      }

      return displayMessages;
   }

   /**
    * Helper class to upload translation files.
    */
   public static class TranslationFileUploadHelper
   {
      private String docId;

      private InputStream fileContents;

      private String fileName;

      private boolean mergeTranslations = true; // Merge by default


      public String getDocId()
      {
         return docId;
      }

      public void setDocId(String docId)
      {
         this.docId = docId;
      }

      public InputStream getFileContents()
      {
         return fileContents;
      }

      public void setFileContents(InputStream fileContents)
      {
         this.fileContents = fileContents;
      }

      public String getFileName()
      {
         return fileName;
      }

      public void setFileName(String fileName)
      {
         this.fileName = fileName;
      }

      public boolean getMergeTranslations()
      {
         return mergeTranslations;
      }

      public void setMergeTranslations(boolean mergeTranslations)
      {
         this.mergeTranslations = mergeTranslations;
      }
   }

   /**
    * Helper class to upload documents.
    */
   public static class DocumentFileUploadHelper
   {
      @Getter
      @Setter
      private InputStream fileContents;

      @Getter
      @Setter
      private String fileName;

      @Getter
      @Setter
      private String documentPath;

      @Getter
      @Setter
      private String sourceLang = "en-US"; // en-US by default
   }
}
