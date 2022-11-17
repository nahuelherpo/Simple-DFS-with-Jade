import jade.core.*;
import java.util.HashMap;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AgenteMovil extends Agent
{
	//operación y nombre del archivo.
	private String operation = "";
	private String fileName = "";
	//Ubicacion o destino del archivo
	private String fileLocation = null;
	//Para controlar la lectura y escritura del archivo.
	private int position = 0;
	private int read_size = 4096;
	private byte[] data = null;
	private int bytes_read;
	private long fileSize = -1;
	//Verifica si finaliza la operación.
	private boolean finish = false;
	private int cont = 0;

	/*Para saber a donde debo migrar el agente usamos un HashMap, el
	cual para cada contenedor indica cual es el siguiente.*/
	private HashMap<String, String> containersChain = new HashMap<String, String>();
	private HashMap<String, String> filesOfServer = new HashMap<String, String>();

	private boolean debug = true;

	private boolean finded = false;

	private boolean operationCompleted = false;

	//Secondaries functions

	private void initializeHashMap() {
		//Hay tres contenedores y un contenedor principal
		this.filesOfServer.put("prac4.pdf", "DataServer1");
		this.filesOfServer.put("soy_texto.txt", "DataServer2");
		this.filesOfServer.put("otro_text.txt", "DataServer2");
	}

	/*En nuestro caso este método se va a ejecutar solo en el contenedor cliente,
	el cual necesita leer o escribir en el DFS.*/
	public void setup() {

		//Tomo los argumentos
		Object[] args = getArguments();

		Location origen = here();

		if (debug) {
			System.out.println("\nSETUP\n");
			System.out.println("\nHola, soy el agente migrado mi nombre local es " + getLocalName());
			System.out.println("Mi nombre completo... " + getName());
			System.out.println("Estoy en la location " + origen.getID() + "\n");
		}

		//Inicializo el HashMap con los nombres de los distintos contenedores
		this.initializeHashMap();

		//Setup of FS configuration selected --PEN--
		try {
			//Tomo los argumentos, la operacion y el nombre del archivo
			this.operation = args[0].toString();
			this.fileName = args[1].toString();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: Missing arguments");
			System.exit(0);
		}

		/*Migro el agente al contenedor ServerDFS, el cual atendera mi solicitud*/
		try {
			ContainerID destino = new ContainerID("ServerDFS", null);
			System.out.println("Migrando el agente a " + destino.getID());
			doMove(destino);
		} catch (Exception e) {
			System.out.println("\nNo fue posible migrar el agente\n");
		}

	}

	/* Este metodo se ejecuta al llegar a un contenedor como resultado de una migracin */
	protected void afterMove() {

		Location location = here();

		if (debug) {
			System.out.println("\nAFTER_MOVE\n");
			System.out.println("\nHola, soy el agente migrado mi nombre local es " + getLocalName());
			System.out.println("Mi nombre completo... " + getName());
			System.out.println("Estoy en la location " + location.getID() + "\n");
		}

		if (location.getName().equals("ServerDFS")) {
			if (debug)
				System.out.println("Estoy en el ServerDFS");
			if (this.operation.equals("-r")) {
				//Chequeo si el archivo existe
				if (this.filesOfServer.keySet().contains(fileName)) {
					//Si existe me fijo en que contenedor esta
					this.fileLocation = this.filesOfServer.get(fileName);
					System.out.println("El archivo existe y esta en el contenedor " + this.fileLocation);
					//Ahora me voy al DataServer para empezar a leer
					ContainerID destino = new ContainerID(this.fileLocation, null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				} else {
					System.out.println("The file does not exist");
					ContainerID destino = new ContainerID("Client", null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				}
			} else {
				//Si no es de lectura la operacion es de escritura
				//Verifico que el archivo no exista, en tal caso decido en donde guardarlo
				if (!this.filesOfServer.keySet().contains(fileName)) {
					//Uso la clase random para mandar el archivo a un server de datos al azar
					if ((new Random()).nextInt(2) == 0) {
						this.fileLocation = "DataServer1";
						System.out.println("0");
					} else {
						this.fileLocation = "DataServer2";
						System.out.println("1");
					}
					//System.out.println("El archivo existe y esta en el contenedor " + this.filesOfServer.get(fileName));
				} else {
					System.out.println("The file already exists");
					ContainerID destino = new ContainerID("Client", null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				}
			}
		} else {
			if (location.getName().equals("DataServer1") || location.getName().equals("DataServer2")) {
				if (this.operation.equals("-r")) {
					//Es una operacion de lectura sobre el server
					//leo el archivo
					this.read(location.getName() + "LocalSpace/" + fileName);
					//volver al cliente
					ContainerID destino = new ContainerID("Client", null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				} else {
					//Es una operacion de escritura sobre el server
				}
			} else {
				//Es el contenedor Cliente
				if (this.operation.equals("-r")) {
					//Es una operacion de lectura sobre el server
					if (!(bytes_read == 0)) {
						//Escribo
						this.write(location.getName() + "LocalSpace/" + fileName);
						//Me fijo si la operacion termino....
						if (!finish) {
							//Vuelvo al DataServer a buscar mas
							ContainerID destino = new ContainerID(this.fileLocation, null);
							System.out.println("Migrando el agente a " + destino.getID());
							doMove(destino);
						} else {
							System.out.println("Lectura completada con exito");
						}
					} else {
						System.out.println("El archivo no existe");
					}
				} else {
					//Es una operacion de escritura sobre el server
				}
			}
		}

	}

	/**
	 * Lee en bytes la cantidad de datos indicada por la variable read_size el archivo 
	 * que se encuentra en origin_path y asigna los datos leidos en la variable data.
	 */
	private void read(String filePath){
        bytes_read = 0;
        try {
            InputStream inStream = new FileInputStream(filePath);
            data = new byte[read_size];
            inStream.skip(position);
            bytes_read = inStream.read(data, 0, read_size);
            inStream.close();
        } catch(IOException e) {
        	System.out.println("Error occurred: " + e.getMessage());
        }
    }

	/**
	 * Escribe en bytes la cantidad de datos indicada por la variable read_size sobre el
	 * archivo que se encuentra en destiny_path los datos indicados en la variable data.
	 */
    private void write(String filePath){
        try {
        	File file = new File(filePath);
        	FileOutputStream outStream = new FileOutputStream(file, file.exists());
            if (bytes_read > 0) {
            	outStream.write(data, 0, bytes_read);
                data = null;
                outStream.close();
                position = position + bytes_read;
            } else {
                finish = true;
            }
        } catch (Exception e) {
        	System.out.println("Error occurred: "+ e.getMessage());
        }
    }

}
