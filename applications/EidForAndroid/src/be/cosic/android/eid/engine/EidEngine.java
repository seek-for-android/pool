/*
 * Copyright 2010 Gauthier Van Damme for COSIC
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package be.cosic.android.eid.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import android.content.Context;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.view.Gravity;
import android.widget.Toast;
import be.cosic.android.eid.exceptions.InvalidPinException;
import be.cosic.android.eid.exceptions.InvalidResponse;
import be.cosic.android.eid.exceptions.NoSuchFileException;
import be.cosic.android.eid.interfaces.EidCommandsInterface;
import be.cosic.android.util.MathUtils;
import be.cosic.android.util.TextUtils;


public class EidEngine implements EidCommandsInterface{

	
	public static final String LOG_TAG = "@string/log_tag";
	
	
	
	//private ObjectFactory of;
	//private MasterFile mf;
	
	
	public static ICardChannel cardChannel;
	
	
	
	public Hashtable<String,String> identityInfo = new Hashtable<String, String>();
	public Hashtable<String,String> addressInfo = new Hashtable<String, String>();


	private Document doc;
	private Element mf;
	
	private Element dirFile;
	private Element dirFileData;
	
	private Element belPicDirectory;
	
	private Element objectDirectoryFile;
	private Element objectDirectoryFileData;
	private Element tokenInfo;
	private Element tokenInfoFileData;
	private Element authenticationObjectDirectoryFile;
	private Element authenticationObjectDirectoryFileData;
	private Element privateKeyDirectoryFile;
	private Element privateKeyDirectoryFileData;
	private Element certificateDirectoryFile;
	private Element certificateDirectoryFileData;
	private Element authenticationCertificate;
	private Element authenticationCertificateFileData;
	private Element nonRepudiationCertificate;
	private Element nonRepudiationCertificateFileData;
	private Element caCertificate;
	private Element caCertificateFileData;
	private Element rootCaCertificate;
	private Element rootCaCertificateFileData;
	private Element rrnCertificate;
	private Element rrnCertificateFileData;
	
	private Element IDDirectory;
	
	private Element identityFile;
	private Element identityFileData;
	private Element identityFileSignature;
	private Element identityFileSignatureFileData;
	private Element addressFile;
	private Element addressFileData;
	private Element addressFileSignature;
	private Element addressFileSignatureFileData;
	private Element photoFile;
	private Element photoFileData;
	private Element caRoleIDFile;
	private Element caRoleIDFileData;
	private Element preferencesFile;
	private Element preferencesFileData;
	
	
   
   
	
	
	
	public EidEngine() {

		
		
		
		//Create instance of DocumentBuilderFactory
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    //Get the DocumentBuilder
	    DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
		
		    //Create blank DOM Document
		    doc = docBuilder.newDocument();
	       
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    //create the root element
	    mf = doc.createElement("ns2:MasterFile");
	    //all it to the xml tree
	    doc.appendChild(mf);
	    
	      
	      //create child element
	      dirFile = doc.createElement("dirFile");
	      //Add the atribute to the child
	      //dirFile.setAttribute("attribute1","The value of Attribute 1");
	      mf.appendChild(dirFile);
	      	
	      	dirFileData = doc.createElement("fileData");
	      	dirFile.appendChild(dirFileData);
	      		
	      		Text fileData = doc.createTextNode("value");
	      		dirFileData.appendChild(fileData);
	    
	      belPicDirectory = doc.createElement("belPicDirectory");
	      mf.appendChild(belPicDirectory);
	      
		      	objectDirectoryFile = doc.createElement("objectDirectoryFile");
		      	belPicDirectory.appendChild(objectDirectoryFile);
		      	
		      		objectDirectoryFileData = doc.createElement("fileData");
		      		objectDirectoryFile.appendChild(objectDirectoryFileData);	
		      	
		      		fileData = doc.createTextNode("value");
		      		objectDirectoryFileData.appendChild(fileData);
		      	
				tokenInfo = doc.createElement("tokenInfo");
		      	belPicDirectory.appendChild(tokenInfo);
		      	
		      		tokenInfoFileData = doc.createElement("fileData");
		      		tokenInfo.appendChild(tokenInfoFileData);	
		      	
		      		fileData = doc.createTextNode("value");
		      		tokenInfoFileData.appendChild(fileData);
		      			
		      	      			
		  		authenticationObjectDirectoryFile = doc.createElement("authenticationObjectDirectoryFile");
		    	belPicDirectory.appendChild(authenticationObjectDirectoryFile);
		    	
		    		authenticationObjectDirectoryFileData = doc.createElement("fileData");
		    		authenticationObjectDirectoryFile.appendChild(authenticationObjectDirectoryFileData);	
		    	
		    		fileData = doc.createTextNode("value");
		    		authenticationObjectDirectoryFileData.appendChild(fileData);
		      		
		    			
				privateKeyDirectoryFile = doc.createElement("privateKeyDirectoryFile");
		      	belPicDirectory.appendChild(privateKeyDirectoryFile);
		      	
		      		privateKeyDirectoryFileData = doc.createElement("fileData");
		      		privateKeyDirectoryFile.appendChild(privateKeyDirectoryFileData);	
		      	
		      		fileData = doc.createTextNode("value");
		      		privateKeyDirectoryFileData.appendChild(fileData);
		      		
		      			
		  		certificateDirectoryFile = doc.createElement("certificateDirectoryFile");
		    	belPicDirectory.appendChild(certificateDirectoryFile);
		    	
		    		certificateDirectoryFileData = doc.createElement("fileData");
		    		certificateDirectoryFile.appendChild(certificateDirectoryFileData);	
		    	
		    		fileData = doc.createTextNode("value");
		    		certificateDirectoryFileData.appendChild(fileData);
		      		
		    			
				authenticationCertificate = doc.createElement("authenticationCertificate");
		      	belPicDirectory.appendChild(authenticationCertificate);
		      	
		      		authenticationCertificateFileData = doc.createElement("fileData");
		      		authenticationCertificate.appendChild(authenticationCertificateFileData);	
		      	
		      		fileData = doc.createTextNode("value");
		      		authenticationCertificateFileData.appendChild(fileData);
		      		
		      			
		  		nonRepudiationCertificate = doc.createElement("nonRepudiationCertificate");
		    	belPicDirectory.appendChild(nonRepudiationCertificate);
		    	
		    		nonRepudiationCertificateFileData = doc.createElement("fileData");
		    		nonRepudiationCertificate.appendChild(nonRepudiationCertificateFileData);	
		    	
		    		fileData = doc.createTextNode("value");
		    		nonRepudiationCertificateFileData.appendChild(fileData);
		      		
		    			
				caCertificate = doc.createElement("caCertificate");
		      	belPicDirectory.appendChild(caCertificate);
		      	
		      		caCertificateFileData = doc.createElement("fileData");
		      		caCertificate.appendChild(caCertificateFileData);	
		      	
		      		fileData = doc.createTextNode("value");
		      		caCertificateFileData.appendChild(fileData);
		      		
		      			
		  		rootCaCertificate = doc.createElement("rootCaCertificate");
		    	belPicDirectory.appendChild(rootCaCertificate);
		    	
		    		rootCaCertificateFileData = doc.createElement("fileData");
		    		rootCaCertificate.appendChild(rootCaCertificateFileData);	
		    	
		    		fileData = doc.createTextNode("value");
		    		rootCaCertificateFileData.appendChild(fileData);
		      		
		    			
				rrnCertificate = doc.createElement("rrnCertificate");
		      	belPicDirectory.appendChild(rrnCertificate);
		      	
		      		rrnCertificateFileData = doc.createElement("fileData");
		      		rrnCertificate.appendChild(rrnCertificateFileData);	
		    	        	        	        	      	
		      		fileData = doc.createTextNode("value");
		      		rrnCertificateFileData.appendChild(fileData);
		      		
		  
	      
		IDDirectory = doc.createElement("IDDirectory");
	    mf.appendChild(IDDirectory);
	      
			    identityFile = doc.createElement("identityFile");
			    IDDirectory.appendChild(identityFile);
			  	
			  		identityFileData = doc.createElement("fileData");
			  		identityFile.appendChild(identityFileData);	
			  	
			  		fileData = doc.createTextNode("value");
			  		identityFileData.appendChild(fileData);
		      		
			  	
				identityFileSignature = doc.createElement("identityFileSignature");
				IDDirectory.appendChild(identityFileSignature);
			  	
			  		identityFileSignatureFileData = doc.createElement("fileData");
			  		identityFileSignature.appendChild(identityFileSignatureFileData);	
			  	
			  		fileData = doc.createTextNode("value");
			  		identityFileSignatureFileData.appendChild(fileData);
		      		
			  	      			
			  	addressFile = doc.createElement("addressFile");
				IDDirectory.appendChild(addressFile);
				
					addressFileData = doc.createElement("fileData");
					addressFile.appendChild(addressFileData);	
				
					fileData = doc.createTextNode("value");
					addressFileData.appendChild(fileData);
		      		
						
				addressFileSignature = doc.createElement("addressFileSignature");
				IDDirectory.appendChild(addressFileSignature);
			  	
			  		addressFileSignatureFileData = doc.createElement("fileData");
			  		addressFileSignature.appendChild(addressFileSignatureFileData);	
			  	
			  		fileData = doc.createTextNode("value");
			  		addressFileSignatureFileData.appendChild(fileData);
		      		
			  			
				photoFile = doc.createElement("photoFile");
				IDDirectory.appendChild(photoFile);
				
					photoFileData = doc.createElement("fileData");
					photoFile.appendChild(photoFileData);	
				
					fileData = doc.createTextNode("value");
					photoFileData.appendChild(fileData);
		      		
						
				caRoleIDFile = doc.createElement("caRoleIDFile");
				IDDirectory.appendChild(caRoleIDFile);
			  	
			  		caRoleIDFileData = doc.createElement("fileData");
			  		caRoleIDFile.appendChild(caRoleIDFileData);	
			  	
			  		fileData = doc.createTextNode("value");
			  		caRoleIDFileData.appendChild(fileData);
		      		
			  			
				preferencesFile = doc.createElement("preferencesFile");
				IDDirectory.appendChild(preferencesFile);
				
					preferencesFileData = doc.createElement("fileData");
					preferencesFile.appendChild(preferencesFileData);	
				
					fileData = doc.createTextNode("value");
					preferencesFileData.appendChild(fileData);
				  
	     
		
		//Build the local eid file structure
		/*of = new ObjectFactory();
		mf = of.createMasterFile();
		mf.setDirFile(of.createMasterFileDirFile());
		BelPicDirectory bpd = of.createMasterFileBelPicDirectory();
		bpd.setObjectDirectoryFile(of.createMasterFileBelPicDirectoryObjectDirectoryFile());
		bpd.setTokenInfo(of.createMasterFileBelPicDirectoryTokenInfo());
		bpd.setAuthenticationObjectDirectoryFile(of.createMasterFileBelPicDirectoryAuthenticationObjectDirectoryFile());
		bpd.setPrivateKeyDirectoryFile(of.createMasterFileBelPicDirectoryPrivateKeyDirectoryFile());
		bpd.setCertificateDirectoryFile(of.createMasterFileBelPicDirectoryCertificateDirectoryFile());
		bpd.setAuthenticationCertificate(of.createMasterFileBelPicDirectoryAuthenticationCertificate());
		bpd.setNonRepudiationCertificate(of.createMasterFileBelPicDirectoryNonRepudiationCertificate());
		bpd.setCaCertificate(of.createMasterFileBelPicDirectoryCaCertificate());
		bpd.setRootCaCertificate(of.createMasterFileBelPicDirectoryRootCaCertificate());
		bpd.setRrnCertificate(of.createMasterFileBelPicDirectoryRrnCertificate());
		IDDirectory idd = of.createMasterFileIDDirectory();
		idd.setIdentityFile(of.createMasterFileIDDirectoryIdentityFile());
		idd.setIdentityFileSignature(of.createMasterFileIDDirectoryIdentityFileSignature());
		idd.setAddressFile(of.createMasterFileIDDirectoryAddressFile());
		idd.setAddressFileSignature(of.createMasterFileIDDirectoryAddressFileSignature());
		idd.setPhotoFile(of.createMasterFileIDDirectoryPhotoFile());
		idd.setCaRoleIDFile(of.createMasterFileIDDirectoryCaRoleIDFile());
		idd.setPreferencesFile(of.createMasterFileIDDirectoryPreferencesFile());
		mf.setBelPicDirectory(bpd);
		mf.setIDDirectory(idd);*/
	
		
		
	}
	
	
	public void readEid() throws CardException, NoSuchFileException, InvalidResponse, UnsupportedEncodingException {
		
		//As base64 encoding only available form android API8 and above: use hex encoding in xml file:
		((Text) dirFileData.getFirstChild()).setData(TextUtils.hexDump(readDirFile()));
		
		//android.util.Base64 only from API version 8 and later!
		//((Text) dirFileData.getFirstChild()).setData(encodeToString(readDirFile(), android.util.Base64.DEFAULT));
		
		((Text) objectDirectoryFileData.getFirstChild()).setData(TextUtils.hexDump(readObjectDirectoryFile()));
		((Text) tokenInfoFileData.getFirstChild()).setData(TextUtils.hexDump(readTokenInfo()));
		((Text) authenticationObjectDirectoryFileData.getFirstChild()).setData(TextUtils.hexDump(readAuthenticationObjectDirectoryFile()));
		((Text) privateKeyDirectoryFileData.getFirstChild()).setData(TextUtils.hexDump(readPrivateKeyDirectoryFile()));
		((Text) certificateDirectoryFileData.getFirstChild()).setData(TextUtils.hexDump(readCertificateDirectoryFile()));
		((Text) authenticationCertificateFileData.getFirstChild()).setData(TextUtils.hexDump(readAuthCertificateBytes()));
		((Text) nonRepudiationCertificateFileData.getFirstChild()).setData(TextUtils.hexDump(readNonRepCertificateBytes()));
		((Text) caCertificateFileData.getFirstChild()).setData(TextUtils.hexDump(readCACertificateBytes()));
		((Text) rootCaCertificateFileData.getFirstChild()).setData(TextUtils.hexDump(readRootCACertificateBytes()));
		((Text) rrnCertificateFileData.getFirstChild()).setData(TextUtils.hexDump(readRRNCertificateBytes()));
		
		
		((Text) identityFileData.getFirstChild()).setData(TextUtils.hexDump(readCitizenIdentityDataBytes()));
		((Text) identityFileSignatureFileData.getFirstChild()).setData(TextUtils.hexDump(readIdentityFileSignatureBytes()));
		((Text) addressFileData.getFirstChild()).setData(TextUtils.hexDump(readCitizenAddressBytes()));
		((Text) addressFileSignatureFileData.getFirstChild()).setData(TextUtils.hexDump(readAddressFileSignatureBytes()));
		((Text) photoFileData.getFirstChild()).setData(TextUtils.hexDump(readCitizenPhotoBytes()));
		((Text) caRoleIDFileData.getFirstChild()).setData(TextUtils.hexDump(readCaRoleIDFile()));
		((Text) preferencesFileData.getFirstChild()).setData(TextUtils.hexDump(readPreferencesFile()));
		
		
		
		
		
		
		
		
		
		
		
		/*//android.util.Base64 only from API version 8 and later!
		((Text) dirFileData.getFirstChild()).setData(Base64.encode(readDirFile()));
		//((Text) dirFileData.getFirstChild()).setData(encodeToString(readDirFile(), android.util.Base64.DEFAULT));
		
		((Text) objectDirectoryFileData.getFirstChild()).setData(Base64.encode(readObjectDirectoryFile()));
		((Text) tokenInfoFileData.getFirstChild()).setData(Base64.encode(readTokenInfo()));
		((Text) authenticationObjectDirectoryFile.getFirstChild()).setData(Base64.encode(readAuthenticationObjectDirectoryFile()));
		((Text) privateKeyDirectoryFileData.getFirstChild()).setData(Base64.encode(readPrivateKeyDirectoryFile()));
		((Text) certificateDirectoryFileData.getFirstChild()).setData(Base64.encode(readCertificateDirectoryFile()));
		((Text) authenticationCertificateFileData.getFirstChild()).setData(Base64.encode(readAuthCertificateBytes()));
		((Text) nonRepudiationCertificateFileData.getFirstChild()).setData(Base64.encode(readNonRepCertificateBytes()));
		((Text) caCertificateFileData.getFirstChild()).setData(Base64.encode(readCACertificateBytes()));
		((Text) rootCaCertificateFileData.getFirstChild()).setData(Base64.encode(readRootCACertificateBytes()));
		((Text) rrnCertificateFileData.getFirstChild()).setData(Base64.encode(readRRNCertificateBytes()));
		
		
		((Text) identityFileData.getFirstChild()).setData(Base64.encode(readCitizenIdentityDataBytes()));
		((Text) identityFileSignatureFileData.getFirstChild()).setData(Base64.encode(readIdentityFileSignatureBytes()));
		((Text) addressFileData.getFirstChild()).setData(Base64.encode(readCitizenAddressBytes()));
		((Text) addressFileSignatureFileData.getFirstChild()).setData(Base64.encode(readAddressFileSignatureBytes()));
		((Text) photoFileData.getFirstChild()).setData(Base64.encode(readCitizenPhotoBytes()));
		((Text) caRoleIDFileData.getFirstChild()).setData(Base64.encode(readCaRoleIDFile()));
		((Text) preferencesFileData.getFirstChild()).setData(Base64.encode(readPreferencesFile()));
		*/
		
		
		/*// Call all read methods and put all results in xml masterfile
		// As either all data will come from an existing card or will be set
		// using the set methods,
		// and in these set methods dependencies over different files are
		// checked upon, we do not need to do this anymore here.
		mf.getDirFile().setFileData(readDirFile());
		mf.getBelPicDirectory().getObjectDirectoryFile().setFileData(this.readObjectDirectoryFile());
		mf.getBelPicDirectory().getTokenInfo().setFileData(this.readTokenInfo());
		mf.getBelPicDirectory().getAuthenticationObjectDirectoryFile().setFileData(this.readAuthenticationObjectDirectoryFile());
		mf.getBelPicDirectory().getPrivateKeyDirectoryFile().setFileData(this.readPrivateKeyDirectoryFile());
		mf.getBelPicDirectory().getCertificateDirectoryFile().setFileData(this.readCertificateDirectoryFile());
		mf.getBelPicDirectory().getAuthenticationCertificate().setFileData(this.readAuthCertificateBytes());
		mf.getBelPicDirectory().getNonRepudiationCertificate().setFileData(this.readNonRepCertificateBytes());
		mf.getBelPicDirectory().getCaCertificate().setFileData(this.readCACertificateBytes());
		mf.getBelPicDirectory().getRootCaCertificate().setFileData(this.readRootCACertificateBytes());
		mf.getBelPicDirectory().getRrnCertificate().setFileData(this.readRRNCertificateBytes());
		mf.getIDDirectory().getIdentityFile().setFileData(this.readCitizenIdentityDataBytes());
		mf.getIDDirectory().getIdentityFileSignature().setFileData(this.readIdentityFileSignatureBytes());
		mf.getIDDirectory().getAddressFile().setFileData(this.readCitizenAddressBytes());
		mf.getIDDirectory().getAddressFileSignature().setFileData(this.readAddressFileSignatureBytes());
		mf.getIDDirectory().getPhotoFile().setFileData(this.readCitizenPhotoBytes());
		mf.getIDDirectory().getCaRoleIDFile().setFileData(this.readCaRoleIDFile());
		mf.getIDDirectory().getPreferencesFile().setFileData(this.readPreferencesFile());*/
		
	}
	
	
	public void storeEid(String path) throws IOException{
		
		//TODO as for Base64, tranform is also only available from android API 8 and up
		//So we use hex dump to write data to the xml file: TODO check this with eid quick key toolset if possible --> yes id schem used as here, or possibly if using jaxbe and changing element type to something else then base64
		
		//We need to have an empty xml file as we can not use xml tranformations:
		//or we can write a file from scratch....
		String file = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "\n" +
				"<ns2:MasterFile xmlns:ns2=\"http://fedict.be/eidtoolset/eidlibrary\">" + "\n" +
				"	 <dirFile>" + "\n" +
				"        <fileData>"+   ((Text) dirFileData.getFirstChild()).getData()    +"</fileData>" + "\n" +
				"	 </dirFile>" + "\n" +
				"    <BelPicDirectory>" + "\n" +
				"        <objectDirectoryFile>" + "\n" +
				"            <fileData>" + ((Text) objectDirectoryFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </objectDirectoryFile>" + "\n" +
				"        <tokenInfo>" + "\n" +
				"            <fileData>" + ((Text) tokenInfoFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </tokenInfo>" + "\n" +
				"        <authenticationObjectDirectoryFile>" + "\n" +
				"            <fileData>" + ((Text) authenticationObjectDirectoryFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </authenticationObjectDirectoryFile>" + "\n" +
				"        <privateKeyDirectoryFile>" + "\n" +
				"            <fileData>" + ((Text) privateKeyDirectoryFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </privateKeyDirectoryFile>" + "\n" +
				"        <certificateDirectoryFile>" + "\n" +
				"            <fileData>" + ((Text) certificateDirectoryFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </certificateDirectoryFile>" + "\n" +
				"        <authenticationCertificate>" + "\n" +
				"            <fileData>" + ((Text) authenticationCertificateFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </authenticationCertificate>" + "\n" +
				"        <nonRepudiationCertificate>" + "\n" +
				"            <fileData>" + ((Text) nonRepudiationCertificateFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </nonRepudiationCertificate>" + "\n" +
				"        <caCertificate>" + "\n" +
				"            <fileData>" + ((Text) caCertificateFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </caCertificate>" + "\n" +
				"        <rootCaCertificate>" + "\n" +
				"            <fileData>" + ((Text) rootCaCertificateFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </rootCaCertificate>" + "\n" +
				"        <rrnCertificate>" + "\n" +
				"            <fileData>" + ((Text) rrnCertificateFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </rrnCertificate>" + "\n" +
				"    </BelPicDirectory>" + "\n" +
				"    <IDDirectory>" + "\n" +
				"        <identityFile>" + "\n" +
				"            <fileData>" + ((Text) identityFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </identityFile>" + "\n" +
				"        <identityFileSignature>" + "\n" +
				"            <fileData>" + ((Text) identityFileSignatureFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </identityFileSignature>" + "\n" +
				"        <addressFile>" + "\n" +
				"            <fileData>" + ((Text) addressFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </addressFile>" + "\n" +
				"        <addressFileSignature>" + "\n" +
				"            <fileData>" + ((Text) addressFileSignatureFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </addressFileSignature>" + "\n" +
				"        <photoFile>" + "\n" +
				"            <fileData>" + ((Text) photoFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </photoFile>" + "\n" +
				"        <caRoleIDFile>" + "\n" +
				"            <fileData>" + ((Text) caRoleIDFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </caRoleIDFile>" + "\n" +
				"        <preferencesFile>" + "\n" +
				"            <fileData>" + ((Text) preferencesFileData.getFirstChild()).getData() + "</fileData>" + "\n" +
				"        </preferencesFile>" + "\n" +
				"    </IDDirectory>" + "\n" +
				"</ns2:MasterFile>";
		
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(file.getBytes());
		fos.close();
		
		// As for API version 8 and later:
		/*
		TransformerFactory tranFactory = TransformerFactory.newInstance(); 
	    Transformer aTransformer;
		
			aTransformer = tranFactory.newTransformer();
		

		    Source src = new DOMSource(doc); 
		    Result dest = new StreamResult(new FileOutputStream(path)); 
		    aTransformer.transform(src, dest); 
	    
//	    FileOutputStream fos = new FileOutputStream(path);
//		fos.write(currentCert.getEncoded());
//		fos.close();
//	    
*/		
	}
	
	public void loadEid(String path) throws IOException {
		
		//TODO as for Base64, tranform is also only available from android API 8 and up
		//So we use hex dump to write data to the xml file: TODO check this with eid quick key toolset if possible --> yes id schem used as here, or possibly if using jaxbe and changing element type to something else then base64
		
		// To counter this, and as the xml structure is rather simple, we will just read out the xml file line by line
		// Also we will need to use hex encoding in the xml file as base64 encoding is not supported: TODO check this everywhere!
		// Read in the xml file
        String[] text = TextUtils.readTextFile(path);
        String file = "";
        // Go through every line of the file and put everything in one string
        for (int i = 0; i < text.length; i++) {
        	
        	file = file + text[i] + "\n";
        	
        }
        
        
        int start = 0;
        // Search the file for keywords and set the data accordingly: data is found between <fileData> and </fileData> and the order of appearance is fixed
        // Use util.base64 as base64 only supported as from API v8
       
        
        // TODO: use own base64 encoder/decoder? or just use hexdump? --> hexdump definitely better
        //base64:
//        byte[] toDecode = file.substring(file.indexOf("<fileData>", start), file.lastIndexOf("</fileData>", start)).getBytes();
//        ((Text) dirFileData.getFirstChild()).setData(TextUtils.hexDump(Base64.decode(toDecode,0, toDecode.length, 0)));
//        start = file.lastIndexOf("</fileData>", start);
        // Base64 as for API version 8 and later:
		//((Text) dirFileData.getFirstChild()).setData(encodeToString(readDirFile(), android.util.Base64.DEFAULT));
		
        //hexdump:
        start = file.indexOf("<fileData>", start);
        ((Text) dirFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        
		((Text) objectDirectoryFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
		start = file.indexOf("<fileData>", start + 1);
        ((Text) tokenInfoFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) authenticationObjectDirectoryFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) privateKeyDirectoryFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) certificateDirectoryFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) authenticationCertificateFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) nonRepudiationCertificateFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) caCertificateFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) rootCaCertificateFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) rrnCertificateFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        
		
		((Text) identityFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
		start = file.indexOf("<fileData>", start + 1);
        ((Text) identityFileSignatureFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) addressFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) addressFileSignatureFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) photoFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) caRoleIDFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        ((Text) preferencesFileData.getFirstChild()).setData(file.substring(file.indexOf("<fileData>", start) + 10, file.indexOf("</", start)));
        start = file.indexOf("<fileData>", start + 1);
        
		
		
        
        //TextUtils.writeTextFile(text, path);
        
        identityInfo.clear();
	    addressInfo.clear();
	    parseEidData();
		
		/*TransformerFactory tranFactory = TransformerFactory.newInstance(); 
	    Transformer aTransformer;
		
		aTransformer = tranFactory.newTransformer();
		

	    Source src = new StreamSource(new FileInputStream(path)); 
	    Result dest = new DOMResult(doc); 
	    aTransformer.transform(src, dest); //TODO make this work
	    
	    identityInfo.clear();
	    addressInfo.clear();
	    
	  //set all new values of doc!!!
	    parseEidData();
	    */
//	    
	}


	public void connect(SmartcardClient smartcard) throws CardException{
		//try {
        	//Set up a card channel to the eid applet on the secure element
    		
			cardChannel = smartcard.openBasicChannel(
    		      "Mobile Security Card 00 00");
			cardChannel.transmit(selectAID_APDU);
    		
			
//		} catch (CardException e) {
//			Log.e(LOG_TAG, "Exception in opening basic channel: " + e.getMessage());
//		}
		
		//TODO check this: preferredSmartCardReader = defaultPreferredSmartCardReader;
	}




//	public MasterFile getMF() {
//		return mf;
//	}
	
	
	public Document getDocument() {
		return doc;
	}
	
	public Element getAuthenticationCertificateFileData() {
		return authenticationCertificateFileData;
	}

	
	public Element getNonRepudiationCertificateFileData() {
		return nonRepudiationCertificateFileData;
	}

	public Element getCaCertificateFileData() {
		return caCertificateFileData;
	}

	public Element getRootCaCertificateFileData() {
		return rootCaCertificateFileData;
	}

	public Element getRrnCertificateFileData() {
		return rrnCertificateFileData;
	}

	public Element getPhotoFileData() {
		return photoFileData;
	}

	
	
	

	public byte[] getCardInfo() throws CardException {
		
		byte[] data = cardChannel.transmit(getCardDataCommand);
		byte[] response = new byte[data.length - 2];
		
		System.arraycopy(data, 0, response, 0, response.length);
		
		return response;
	}

	
	public void pinValidationEngine(String PIN) throws InvalidPinException, CardException{
		

		byte[] result = cardChannel.transmit(insertPinIntoApdu(verifyPinApdu, ApduHeaderLength, PIN));
		if (TextUtils.hexDump(result).equals("9000")) {
			// pinPad.setStatusText("OK...");
			
		} else 
			throw new InvalidPinException((result[1] & (byte)0x0F)+"");
	}
	
	
	public void changeThisPin(String currentpinvalue, String newpinvalue) throws CardException, InvalidPinException {
		byte[] result = cardChannel.transmit(insertTwoPinsIntoApdu(changePinApdu, currentpinvalue, newpinvalue));
		if (TextUtils.hexDump(result).equals("9000")) {
			// pinPad.setStatusText("OK...");
			
		} else 
			throw new InvalidPinException((result[1] & (byte)0x0F)+"");

}

	
	private byte[] insertPinIntoApdu(byte[] apdu, int pinBlockOffset, String pinvalue) {
		String pinValue = "";
		if (2 * ((int) (pinvalue.length() / 2)) == pinvalue.length())
			pinValue = pinvalue;
		else
			pinValue = pinvalue + "F";
		byte[] newApdu = new byte[apdu.length];
		for (int i = 0; i < newApdu.length; i++)
			newApdu[i] = apdu[i];
		int offsetInCommand = pinBlockOffset;
		newApdu[offsetInCommand++] = (byte) (2 * 16 + pinValue.length());
		for (int i = 0; i < pinValue.length(); i += 2) {
			newApdu[offsetInCommand++] = (byte) (Integer.parseInt(pinValue.substring(i, i + 2), 16));
		}
		return newApdu;
	}
	
	private byte[] insertTwoPinsIntoApdu(byte[] apdu, String pin1, String pin2) {
		byte[] tmpApdu = insertPinIntoApdu(apdu, ApduHeaderLength, pin1);
		return insertPinIntoApdu(tmpApdu, ApduHeaderLength + PinBlockLength, pin2);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Parse all the data for the eID.
	 * @throws UnsupportedEncodingException 
	 */
	public void parseEidData() throws UnsupportedEncodingException{
		
		//TODO: no mor efficient way of doing this????
		//TODO: for example just a string array with the tag number/identifier the index in this array?
		
		//Parse the ID data
		//byte[] data = mf.getIDDirectory().getIdentityFile().getFileData();	
		//As base64 encoding only available form android API8 and above: use hex encoding in xml file:
		//byte[] data = TextUtils.hexStringToByteArray(((Text) identityFileData.getFirstChild()).getData());
		byte[] data = TextUtils.hexStringToByteArray(((Text) doc.getElementsByTagName("identityFile").item(0).getFirstChild().getFirstChild()).getData());
		int pos = 0;
		
		int tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		int dataLen = TextUtils.getDataLen(data, pos);
		int lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String cardNumber = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Card Number", cardNumber);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String chipNumber = TextUtils.hexDump(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Chip Number", chipNumber);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String cardValidityBegin = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Card validity start date", cardValidityBegin);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String cardValidityEnd = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Card validity end date", cardValidityEnd);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String cardDeliveryMunicipality = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Card delivery municipality", cardDeliveryMunicipality);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String nationalNumber = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("National Number", nationalNumber);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String name = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Name", name);
		String twoFirstFirstNames = "";
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		if (tagIdentifier == (byte)0x08) {
			pos += TextUtils.tagLen;
			dataLen = TextUtils.getDataLen(data, pos);
			lenLen = TextUtils.getLenLen(data, pos);
			pos += lenLen;
			twoFirstFirstNames = TextUtils.getString(data, pos, dataLen);
			pos += dataLen;
			tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		}
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String firstLetterThirdFirstName = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("First names", twoFirstFirstNames + " " + firstLetterThirdFirstName + ".");
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String nationality = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Nationality", nationality);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String birthLocation = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Birth Location", birthLocation);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String birthDate = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Birth Date", birthDate);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String gender = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Sex", gender);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		if (tagIdentifier == (byte)0x0e) {
			pos += TextUtils.tagLen;
			dataLen = TextUtils.getDataLen(data, pos);
			lenLen = TextUtils.getLenLen(data, pos);
			pos += lenLen;
			String nobleCondition = TextUtils.getString(data, pos, dataLen);
			pos += dataLen;
			identityInfo.put("Noble condition", nobleCondition);
			tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		}
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String documentType = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Document type", documentType);
		tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		if (tagIdentifier == (byte)0x10) {
			pos += TextUtils.tagLen;
			dataLen = TextUtils.getDataLen(data, pos);
			lenLen = TextUtils.getLenLen(data, pos);
			pos += lenLen;
			String specialStatus = TextUtils.getString(data, pos, dataLen);
			pos += dataLen;
			identityInfo.put("Special status", specialStatus);
			tagIdentifier = TextUtils.getTagIdentifier(data, pos);
		}
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String photoHash = TextUtils.hexDump(data, pos, dataLen);
		pos += dataLen;
		identityInfo.put("Hash photo", photoHash);
		
		
		
		
		//Parse the address data
		//data = mf.getIDDirectory().getAddressFile().getFileData();		
		//As base64 encoding only available form android API8 and above: use hex encoding in xml file:
		//data = TextUtils.hexStringToByteArray(((Text) addressFileData.getFirstChild()).getData());
		data = TextUtils.hexStringToByteArray(((Text) doc.getElementsByTagName("addressFile").item(0).getFirstChild().getFirstChild()).getData());
		
		
		pos = 0;
		TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String address = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		addressInfo.put("Address", address);
		TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String zip = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		addressInfo.put("Zip code", zip);
		TextUtils.getTagIdentifier(data, pos);
		pos += TextUtils.tagLen;
		dataLen = TextUtils.getDataLen(data, pos);
		lenLen = TextUtils.getLenLen(data, pos);
		pos += lenLen;
		String municipality = TextUtils.getString(data, pos, dataLen);
		pos += dataLen;
		addressInfo.put("Municipality", municipality);
		
		
		
	}


	
	

	
	
	


	





	private byte[] readPreferencesFile() throws CardException, InvalidResponse, UnsupportedEncodingException {
		
		try{
			return readBinaryFile(selectPreferencesFileCommand);
			
		}catch (NoSuchFileException e){
			//Some eid cards do not have a preference file
			return "None".getBytes("UTF-8");
		}
	}


	private byte[] readCaRoleIDFile() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCaRoleIDFileCommand);
	}

	
	private byte[] readCitizenPhotoBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCitizenPhotoCommand);
	}


	private byte[] readAddressFileSignatureBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectAddressFileSignatureCommand);
	}

	
	private byte[] readCitizenAddressBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCitizenAddressDataCommand);
	}


	private byte[] readCitizenIdentityDataBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCitizenIdentityDataCommand);
	}


	private byte[] readIdentityFileSignatureBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectIdentityFileSignatureCommand);
	}


	private byte[] readRRNCertificateBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectRrnCertificateCommand);
	}


	private byte[] readRootCACertificateBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectRootCaCertificateCommand);
	}
	

	private byte[] readCACertificateBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCaCertificateCommand);
	}


	private byte[] readAuthCertificateBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectAuthenticationCertificateCommand);
	}


	private byte[] readNonRepCertificateBytes() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectNonRepudiationCertificateCommand);
	}


	private byte[] readCertificateDirectoryFile() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectCertificateDirectoryFileCommand);
	}


	private byte[] readPrivateKeyDirectoryFile() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectPrivateKeyDirectoryFileCommand);
	}


	private byte[] readTokenInfo() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectTokenInfoCommand);
	}
	

	private byte[] readObjectDirectoryFile() throws CardException, NoSuchFileException, InvalidResponse {
		return readBinaryFile(selectObjectDirectoryFileCommand);
	}


	private byte[] readAuthenticationObjectDirectoryFile() throws CardException, NoSuchFileException, InvalidResponse {
		return readBinaryFile(selectAuthenticationObjectDirectoryFileCommand);
	}
	
	
	private byte[] readDirFile() throws CardException, NoSuchFileException, InvalidResponse {
		
		return readBinaryFile(selectDirFileCommand);
	}






	public byte[] readBinaryFile(byte[] selectFileCommand) throws CardException, NoSuchFileException, InvalidResponse  {
		int wordLength = 1;
		int blocklength = 0x60;
		wordLength = 1;
		blocklength = 0xf8;
		int len = 0;
		int enough = -5000;
		byte[] certificate = new byte[10000];
		byte[] keyBytes = cardChannel.transmit(selectFileCommand);
		
		if ((keyBytes[keyBytes.length - 2] == (byte) 0x6A) && (keyBytes[keyBytes.length - 1] == (byte) 0x82)){
			throw new NoSuchFileException();
		} else if ((keyBytes[keyBytes.length - 2] != (byte) 0x90) || (keyBytes[keyBytes.length - 1] != 0)){
			throw new InvalidResponse(TextUtils.hexDump(keyBytes));
		}
		
		while (enough < 0) {
			readBinaryBlockCommand[2] = (byte) (len / wordLength / 256);
			readBinaryBlockCommand[3] = (byte) (len / wordLength % 256);
			readBinaryBlockCommand[4] = (byte) blocklength;
			keyBytes = cardChannel.transmit(readBinaryBlockCommand);
			if ((keyBytes[keyBytes.length - 2] == (byte) 0x90) && (keyBytes[keyBytes.length - 1] == 0)) {
				for (int j = 0; j < MathUtils.min(blocklength, keyBytes.length - 2); j++) {
					certificate[len + j] = keyBytes[j];
				}
				if ((keyBytes.length - 2) < blocklength) {
					len += (keyBytes.length - 2);
					enough = 0;
				} else {
					len += blocklength;
					enough = -1;
				}
			} else throw new InvalidResponse(TextUtils.hexDump(keyBytes));
		}
		len = MathUtils.unsignedInt(len);
		byte[] tmp = new byte[len];
		for (int k = 0; k < len; k++)
			tmp[k] = certificate[k];
		return tmp;
	}


	


	
	
	












	



	
	
}
