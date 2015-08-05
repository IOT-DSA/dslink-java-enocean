package esp3.profile.a5.xml;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "case")
public class Case {

	private List<DataField> datafields;
	private String title;
	
	 @Override
	 public boolean equals(Object obj) {
		 if (obj == null) {
			 return false;
		 }
		 if (getClass() != obj.getClass()) {
			 return false;
		 }
		 final Case other = (Case) obj;
		 if (!Objects.equals(this.title, other.title)) {
	            return false;
		 }
		 if (!Objects.equals(this.datafields, other.datafields)) {
	            return false;
		 }
		 return true;
	 }
	 
	 @XmlElement(name = "datafield")
	 public List<DataField> getDatafields() {
		 return datafields;
	 }

	 public void setDatafields(List<DataField> datafields) {
		 this.datafields = datafields;
	 }
	 
	 public String getTitle() {
		 return title;
	 }

	 public void setTitle(String title) {
		 this.title = title;
	 }

	 @Override
	 public int hashCode() {
		 int hash = 7;
		 hash = 41 * hash + Objects.hashCode(this.title);
		 hash = 41 * hash + Objects.hashCode(this.datafields);
		 return hash;
	 }

	 @Override
	 public String toString() {
		 return "Case{" + ", title=" + title + ", datafields=" + datafields + '}';
	 }
}
