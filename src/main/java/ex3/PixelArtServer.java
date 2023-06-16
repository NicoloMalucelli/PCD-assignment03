package ex3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PixelArtServer {

	public static void main(String[] args) {
		try{
			Registry registry = LocateRegistry.getRegistry();

			BrushManager brushManager = new BrushManagerImpl();
			BrushManager brushManagerStub = (BrushManager) UnicastRemoteObject.exportObject(brushManager, 0);
			registry.rebind("brushManager", brushManagerStub);

			PixelGrid grid = new PixelGridImpl(40,40);
			PixelGrid gridStub = (PixelGrid) UnicastRemoteObject.exportObject(grid, 0);
			registry.rebind("grid", gridStub);

			System.out.println("Server online");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

	}

}
