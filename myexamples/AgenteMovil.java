import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jade.core.*;

public class AgenteMovil extends Agent{
	
	private Location origen = null;
	//operación y paths de los archivos.
	private String operation = "";
	private String destiny_path = "";
	private String origin_path = "";
	//Para controlar la lectura y escritura del archivo.
	private int position = 0;
	private int read_size = 1024;
	private byte[] data = null;
    private int bytes_read;
    //Verifica si finaliza la operación.
    private boolean finish = false;
    
    private int cont = 0;
    
    private boolean verifyFileExist(String path) {
    	File file = new File(path);
    	return file.exists();
    }
    
    private void doPaths(String name_file) throws FileNotFoundException {
    	if (operation.equals("-r")) {
    		destiny_path = "myexamples/ClientLocalSpace/copy-"+name_file;
    		origin_path = "myexamples/RepositoryServer/"+name_file;
    	} else {
    		destiny_path = "myexamples/RepositoryServer/new-"+name_file;
        	origin_path = "myexamples/ClientLocalSpace/"+name_file;
    	}
    	if (!this.verifyFileExist(origin_path)) {
    		throw new FileNotFoundException("The file '/"+name_file+"' does not exist in the directory "+origin_path);
    	}
    }
    
	// Ejecutado por unica vez en la creacion
	public void setup(){
		this.origen = here();
		System.out.println("\n\nHola, agente con nombre local " + getLocalName());
		System.out.println("Y nombre completo... " + getName());
		System.out.println("Y en location " + origen.getID() + "\n\n");

		Object[] args = getArguments();
		try {
			operation = args[0].toString();
			this.doPaths(args[1].toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: Missing arguments");
			System.out.println("First argument: Read: -r ------------- Write: -w");
			System.out.println("Second argument: Name file to operate, example: file1.txt");
			System.exit(0);
		} catch (FileNotFoundException e) {
			System.out.println("The file you want to operate on does not exist");
			System.exit(0);
		}

		if(operation.equals("-w")){
			this.read();
		}

		// Para migrar el agente
		try {
			ContainerID destino = new ContainerID("Main-Container", null);
			System.out.println("Migrando el agente a " + destino.getID());
			doMove(destino);
		} catch (Exception e) {
			System.out.println("\n\n\nNo fue posible migrar el agente\n\n\n");
		}
	}
	
	//Ejecutado al llegar a un contenedor como resultado de una migracion.
	protected void afterMove(){
		
		if (cont == 25) {
			cont = 0;
			System.out.print(".");
		}
		cont++;
		
		Location actual = here();
		if(!finish){
			switch (this.selectOperation(actual)) {
			case "-r":
					this.read();
					break;
			case "-w":
					this.write();
					break;	
			default:
					System.out.println("Error: invalid value!!");
					System.out.println("Valid values: Read:'-r' <-----> Write: '-w'");
					System.exit(0);
			}
			this.selectContainerToMove(actual);
		}else{
			if (this.isInMain(actual)) {
				this.doMove(origen);
			}
			System.out.println("\nThe "+operation+ " operation complete!!\nFile transfer completed successfully!!!\n\n");
		}
	}
	
	/**
	 * Lee en bytes la cantidad de datos indicada por la variable read_size el archivo 
	 * que se encuentra en origin_path y asigna los datos leidos en la variable data.
	 */
	private void read(){
        bytes_read = 0;
        try{
            InputStream inStream = new FileInputStream(origin_path);
            data = new byte[read_size];
            inStream.skip(position);
            bytes_read = inStream.read(data, 0, read_size);
            inStream.close();
        }catch(IOException e){
        	System.out.println("Error occurred: "+ e.getMessage());
        }
    }
	
	/**
	 * Escribe en bytes la cantidad de datos indicada por la variable read_size sobre el
	 * archivo que se encuentra en destiny_path los datos indicados en la variable data.
	 */
    private void write(){
        try{
        	File file = new File(destiny_path);
        	FileOutputStream outStream = new FileOutputStream(file,file.exists());
            if (bytes_read > 0) {
            	outStream.write(data, 0, bytes_read);
                data = null;
                outStream.close();
                position = position + bytes_read;
            }else{
                finish = true;
            }
        } catch (Exception e){
        	System.out.println("Error occurred: "+ e.getMessage());
        }
    }
    
    /**
     * 
     * @param actual : Contenedor actual en el que se encuentra el agente.
     * @return : Retorna el resultado de preguntar si el agente se encuentra en el Main-Container.
     */
    private boolean isInMain(Location actual) {
		return !(this.origen.getName().equals(actual.getName()));
	}
	
    /**
     * 
     * @param op : String que indica la operación indicada como argumento del programa.
     * @return : Retorna la operación contraria. Si se indico '-r', retorna '-w' y viseversa.
     */
	private String reverseOperation(String op) {
		if (op.equals("-r")) {
			return "-w";
		}
		return "-r";
	}
	
	/**
	 * 
	 * @param actual : Contenedor actual en el que se encuentra el agente.
	 * @return : Retorna la operación que se debe llevar a cabo dependiendo de en que contenedor se encuentre el agente.
	 */
	private String selectOperation(Location actual) {
		if (!isInMain(actual)) {
			return this.reverseOperation(operation);
		} 
		return operation;
	} 
	
	/**
	 * Selecciona el contenedor al que el agente le corresponde viajar.
	 * @param actual : Contenedor actual en el que se encuentra el agente.
	 */
	private void selectContainerToMove(Location actual) {
		if(!isInMain(actual)){
			ContainerID destino = new ContainerID("Main-Container", null);
			doMove(destino);
		}else{
			doMove(origen);
		}
	}
}
