package org.razib.pdb.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * A Pdb entry header example
 * 
 * <PDB structureId="4HHB" title="THE CRYSTAL STRUCTURE" pubmedId="6726807"
 * expMethod="X-RAY DIFFRACTION" resolution="1.74" replaces="1HHB"
 * keywords="OXYGEN TRANSPORT" nr_entities="4" nr_residues="574" nr_atoms="4779"
 * publish_date="1984-03-07" revision_date="1984-07-17"
 * last_modification_date="2011-07-13"
 * structure_authors="Fermi, G., Perutz, M.F." status="CURRENT"
 * citation_authors="Fermi, G., Perutz, M.F., Shaanan, B., Fourme, R." />
 * 
 * @author raz
 * 
 */
@Root(name = "PDB", strict=false)
public class PdbSummary implements Serializable {
	private static final long serialVersionUID = 1L;
	@Attribute(required=false)
	private String structureId;
	@Attribute(required=false)
	private String title;
	@Attribute(required=false)
	private String pubmedId;
	@Attribute(required=false)
	private String expMethod;
	@Attribute(required=false)
	private String resolution;
	@Attribute(required=false)
	private String replaces;
	@Attribute(required=false)
	private String replacedBy;
	@Attribute(required=false)
	private String keywords;
	@Attribute(required=false)
	private String nr_entities;
	@Attribute(required=false)
	private String nr_residues;
	@Attribute(required=false)
	private String nr_atoms;
	@Attribute(required=false)
	private String publish_date;
	@Attribute(required=false)
	private String revision_date;
	@Attribute(required=false)
	private String last_modification_date;
	@Attribute(required=false)
	private String structure_authors;
	@Attribute(required=false)
	private String citation_authors;	
	@Attribute(required=false)
	private String status;
	
	public String getStructureId() {
		return structureId;
	}

	public void setStructureId(String strutureId) {
		this.structureId = strutureId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public String getExpMethod() {
		return expMethod;
	}

	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getReplaces() {
		return replaces;
	}

	public void setReplaces(String replaces) {
		this.replaces = replaces;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getNr_entities() {
		return nr_entities;
	}

	public void setNr_entities(String nr_entities) {
		this.nr_entities = nr_entities;
	}

	public String getRevision_date() {
		return revision_date;
	}

	public void setRevision_date(String revision_date) {
		this.revision_date = revision_date;
	}

	public String getLast_modification_date() {
		return last_modification_date;
	}

	public void setLast_modification_date(String last_modification_date) {
		this.last_modification_date = last_modification_date;
	}

	public String getStructure_authors() {
		return structure_authors;
	}

	public void setStructure_authors(String structure_authors) {
		this.structure_authors = structure_authors;
	}

	public String getCitation_authors() {
		return citation_authors;
	}

	public void setCitation_authors(String citation_authors) {
		this.citation_authors = citation_authors;
	}

	public String getNr_residues() {
		return nr_residues;
	}

	public void setNr_residues(String nr_residues) {
		this.nr_residues = nr_residues;
	}

	public String getNr_atoms() {
		return nr_atoms;
	}

	public void setNr_atoms(String nr_atoms) {
		this.nr_atoms = nr_atoms;
	}

	public String getPublish_date() {
		return publish_date;
	}

	public void setPublish_date(String publish_date) {
		this.publish_date = publish_date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(String replacedBy) {
		this.replacedBy = replacedBy;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(" \tStructureId: ").append(structureId)
				//.append("\n\t title: ").append(title)
				.append("\n\t Structure Authors: ").append(structure_authors)
				.append("\n\t Status: ").append(status)
				.append("\n\t PubmedId :")
				.append(pubmedId).append("\n\t Experimental Method: ").append(expMethod)
				.append("\n\t Resolution: ").append(resolution)
				.append("\n\t Replaces: ").append(replaces).append("\n\t Replaced By: ")
				.append(replacedBy).append("\n\t Keywords: ").append(keywords)
				.append("\n\t Entities: ").append(nr_entities)
				.append("\n\t Residues: ").append(nr_residues)
				.append("\n\t Atoms: ").append(nr_atoms)
				.append("\n\t Citation Authors: ").append(citation_authors)
				.append("\n\t Publish Date: ").append(publish_date)
				.append("\n\t Revision Date: ").append(revision_date)
				.append("\n\t Last Modification Date: ")
				.append(last_modification_date);
		return builder.toString();
	}

	public String getImageUrl() {
		return "http://www.pdb.org/pdb/images/"+getStructureId()+"_bio_r_250.jpg";
	}

	public String getImageUrlHd() {
		// TODO Auto-generated method stub
		return "http://www.pdb.org/pdb/images/"+getStructureId()+"_bio_r_500.jpg";
	}
	
}
