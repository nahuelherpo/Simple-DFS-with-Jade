import jade.core.*;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AgenteMovil extends Agent
{
	//new variables
	private Location origen = null;
	//operación y paths de los archivos.
	private String operation = "";
	private String destiny_path = "";
	private String origin_path = "";
	private String fileName = "";
	//Para controlar la lectura y escritura del archivo.
	private int position = 0;
	private int read_size = 1024;
	private byte[] data = null;
	private int bytes_read;
	//Verifica si finaliza la operación.
	private boolean finish = false;
	private int cont = 0;

	/*Para saber a donde debo migrar el agente usamos un HashMap, el
	cual para cada contenedor indica cual es el siguiente.*/
	private HashMap<String, String> containersChain = new HashMap<String, String>();

	private boolean debug = true;

	private boolean finded = false;

	private boolean operationCompleted = false;

	//Secondaries functions

	private void initializeHashMap() {
		//Hay tres contenedores y un contenedor principal
		this.containersChain.put("Container-1", "Main-Container");
		this.containersChain.put("Container-2", "Container-1");
		this.containersChain.put("Container-3", "Container-2");
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
			//-r or -w
			this.operation = args[0].toString();
			this.fileName = args[1].toString();
			//mmmmm, esto habria que retrasarlo, ya que no sabemos en qué contenedor está
			//this.doPaths(args[1].toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: Missing arguments");
			System.exit(0);
		}

		/*Migro el agente en el orden de la cadena establecido, a menos que ya
		se haya hecho la operacion, en tal caso voy directo al MainContainer.*/
		//En princio sigue toda la cadena por defecto --PEN--
		try {
			if (this.debug)
				System.out.println("nextContainer " + this.containersChain.get(origen.getID().split("@")[0]));
			ContainerID destino = new ContainerID(this.containersChain.get(origen.getID().split("@")[0]), null);
			System.out.println("Migrando el agente a " + destino.getID());
			doMove(destino);
		} catch (Exception e) {
			System.out.println("\nNo fue posible migrar el agente\n");
		}

	}

	/* Este metodo se ejecuta al llegar a un contenedor como resultado de una migracin */
	protected void afterMove() {

		Location origen = here();

		if (debug) {
			System.out.println("\nAFTER_MOVE\n");
			System.out.println("\nHola, soy el agente migrado mi nombre local es " + getLocalName());
			System.out.println("Mi nombre completo... " + getName());
			System.out.println("Estoy en la location " + origen.getID() + "\n");
		}

		if (!origen.getName().equals("Main-Container")) {

			if (new File("Server" + origen.getID().charAt(10) + "LocalSpace/" + fileName).isFile()) {
				System.out.println("File finded");
				this.finded = true;
			}

			/*Migro el agente en el orden de la cadena establecido, a menos que ya
			se haya hecho la operacion, en tal caso voy directo al MainContainer.*/
			//En princio sigue toda la cadena por defecto --PEN--
			if (!finded) {
				try {
					ContainerID destino = new ContainerID(this.containersChain.get(origen.getID().split("@")[0]), null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				} catch (Exception e) {
					System.out.println("\nNo fue posible migrar el agente\n");
				}
			} else { /* Si ya fue encontrado me voy directo al MainContainer*/
				try {
					ContainerID destino = new ContainerID("Main-Container", null);
					System.out.println("Migrando el agente a " + destino.getID());
					doMove(destino);
				} catch (Exception e) {
					System.out.println("\nNo fue posible migrar el agente\n");
				}
			}

		} else {
			if (debug)
				System.out.println("El agente esta en el MainContainer");
			if (operationCompleted)
				System.out.println("La operacion fue realizada con exito");
			if (!finded)
				System.out.println("Error: Archivo no encontrado");
		}

	}

	/**
	 * Lee en bytes la cantidad de datos indicada por la variable read_size el archivo 
	 * que se encuentra en origin_path y asigna los datos leidos en la variable data.
	 */
	private void read(){
        bytes_read = 0;
        try {
            InputStream inStream = new FileInputStream(origin_path);
            data = new byte[read_size];
            inStream.skip(position);
            bytes_read = inStream.read(data, 0, read_size);
            inStream.close();
        } catch(IOException e){
        	System.out.println("Error occurred: "+ e.getMessage());
        }
    }

	/**
	 * Escribe en bytes la cantidad de datos indicada por la variable read_size sobre el
	 * archivo que se encuentra en destiny_path los datos indicados en la variable data.
	 */
    private void write(){
        try {
        	File file = new File(destiny_path);
        	FileOutputStream outStream = new FileOutputStream(file,file.exists());
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
