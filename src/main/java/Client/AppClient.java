package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppClient extends Application {
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	private ObjectOutputStream toServerObj;
	private ObjectInputStream fromServerObj;
	private TextField tf;
	private TextArea ta;

	public static void main(String[] args) {
		System.out.println("Hello World!");
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		BorderPane paneForTextField = new BorderPane();
		paneForTextField.setPadding(new Insets(5, 5, 5, 5));
		paneForTextField.setStyle("-fx-border-color: green");
		paneForTextField.setLeft(new Label("Enter"));

		tf = new TextField();
		tf.setAlignment(Pos.BOTTOM_RIGHT);
		paneForTextField.setCenter(tf);
		BorderPane mainPane = new BorderPane();
		// Text area to display contents
		ta = new TextArea();
		mainPane.setCenter(new ScrollPane(ta));
		mainPane.setTop(paneForTextField);
		// Create a scene and place it in the stage
		Scene scene = new Scene(mainPane, 480, 230);
		primaryStage.setTitle("Client"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		ta.setEditable(false);
		try {
			Socket socket = new Socket("localhost", 9000);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
			toServerObj = new ObjectOutputStream(socket.getOutputStream());
			fromServerObj = new ObjectInputStream(socket.getInputStream());
			tf.appendText(fromServer.readUTF());
			double clientNo = fromServer.readDouble();
			tf.setOnAction(new SendServer(clientNo));
		} catch (IOException e) {
			ta.appendText(e.getMessage());
		}
	}

	class SendServer implements EventHandler<ActionEvent> {
		private double clientNo;

		SendServer(double clientNo) {
			this.clientNo = clientNo;
		}

		public void handle(ActionEvent arg0) {
			String text = tf.getText().trim();
			try {
				toServer.writeUTF(text);
				toServer.writeDouble(clientNo);
				toServer.flush();
				if (fromServer.readDouble() == 1) {
					Double controlcmd = fromServer.readDouble();
					if (controlcmd == 1) {
						ta.appendText(fromServer.readUTF());
						if (fromServer.readDouble() == 1) {
							if (text.contains("ls")) {
								ArrayList<String> ls = (ArrayList<String>) fromServerObj.readObject();
								for (String list : ls)
									ta.appendText(list + "\n");
								if (ls.size() == 0)
									ta.appendText("Folder is empty\n");
								String path = fromServer.readUTF();
								tf.clear();
								tf.appendText(path);
							}
							if (text.contains("cd ")) {
								String[] s = text.split(" ");
								toServer.writeUTF(s[s.length - 1]);
								Double control = fromServer.readDouble();
								if (control == 1) {
									String path = fromServer.readUTF();
									ta.appendText(path + "\n");
									tf.clear();
									tf.appendText(path);
								} else if (control == 0) {
									String path = fromServer.readUTF();
									ta.appendText("Folder not found\n");
									tf.clear();
									tf.appendText(path);
								} else {
									String path = fromServer.readUTF();
									ta.appendText("Out of the range\n");
									tf.clear();
									tf.appendText(path);
								}
							}
							if (text.contains("pwd")) {
								String path = fromServer.readUTF();
								ta.appendText(path + "\n");
								tf.clear();
								tf.appendText(path);
							}
							if (text.contains("mkdir ")) {
								String[] s = text.split(" ");
								toServer.writeUTF(s[s.length - 1]);
								Double control = fromServer.readDouble();
								if (control == 1) {
									String path = fromServer.readUTF();
									ta.appendText("Create " + s[s.length - 1] + " Folder\n");
									tf.clear();
									tf.appendText(path);
								} else {
									String path = fromServer.readUTF();
									ta.appendText(s[s.length - 1] + " is already exist\n");
									tf.clear();
									tf.appendText(path);
								}
							}
							if (text.contains("rm ")) {
								String[] s = text.split(" ");
								toServer.writeUTF(s[s.length - 1]);
								Double control = fromServer.readDouble();
								if (control == 1) {
									String path = fromServer.readUTF();
									ta.appendText("Delete " + s[s.length - 1] + " Folder\n");
									tf.clear();
									tf.appendText(path);
								} else if (control == 0) {
									String path = fromServer.readUTF();
									ta.appendText(s[s.length - 1] + " is not Found\n");
									tf.clear();
									tf.appendText(path);
								} else {
									String path = fromServer.readUTF();
									ta.appendText(s[s.length - 1]
											+ " cannot delete because Folder is not Empty!, First, you must delete the children\n");
									tf.clear();
									tf.appendText(path);
								}
							}
							if (text.contains("write ") || text.contains("read ")) {
								String[] s = text.split(" ");
								toServer.writeUTF(s[s.length - 2]);
								toServer.writeUTF(s[s.length - 1]);
								if(fromServer.readDouble() == 1) {
									byte[] bytes = new byte[(int)fromServer.readDouble()];
									fromServer.read(bytes);
									FileOutputStream dos = new FileOutputStream(s[s.length - 1]);
									dos.write(bytes);
								}
								ta.appendText(fromServer.readUTF());
								tf.clear();
								tf.appendText(fromServer.readUTF());
							}
						} else {
							ta.appendText("Path is wrong! \n");
							String path = fromServer.readUTF();
							tf.clear();
							tf.appendText(path);
						}

					} else {
						ta.appendText(fromServer.readUTF());
						tf.clear();
						tf.appendText(fromServer.readUTF());
					}
				} else {
					tf.clear();
					tf.appendText(fromServer.readUTF());
					ta.appendText("Out of the Range\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}