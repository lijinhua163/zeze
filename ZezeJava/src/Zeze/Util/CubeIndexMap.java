package Zeze.Util;

import Zeze.*;
import java.util.*;

/** 
 把三维空间划分成一个个相邻的Cube。
 地图中的玩家或者物品Id记录在所在的Cube中。
 用来快速找到某个坐标周围的玩家或物体。
*/
//C# TO JAVA CONVERTER TODO TASK: The C# 'new()' constraint has no equivalent in Java:
//ORIGINAL LINE: public class CubeIndexMap<TCube, TObject> where TCube : Cube<TObject>, new()
public class CubeIndexMap<TCube extends Cube<TObject>, TObject> {
	private java.util.concurrent.ConcurrentHashMap<CubeIndex, TCube> Cubes = new java.util.concurrent.ConcurrentHashMap<CubeIndex, TCube>();

	private int CubeSizeX;
	public final int getCubeSizeX() {
		return CubeSizeX;
	}
	private int CubeSizeY;
	public final int getCubeSizeY() {
		return CubeSizeY;
	}
	private int CubeSizeZ;
	public final int getCubeSizeZ() {
		return CubeSizeZ;
	}

	public final CubeIndex ToIndex(double x, double y, double z) {
		CubeIndex tempVar = new CubeIndex();
		tempVar.setX((long)(x / getCubeSizeX()));
		tempVar.setY((long)(y / getCubeSizeY()));
		tempVar.setZ((long)(z / getCubeSizeZ()));
		return tempVar;
	}

	public final CubeIndex ToIndex(float x, float y, float z) {
		CubeIndex tempVar = new CubeIndex();
		tempVar.setX((long)(x / getCubeSizeX()));
		tempVar.setY((long)(y / getCubeSizeY()));
		tempVar.setZ((long)(z / getCubeSizeZ()));
		return tempVar;
	}

	public final CubeIndex ToIndex(long x, long y, long z) {
		CubeIndex tempVar = new CubeIndex();
		tempVar.setX((long)(x / getCubeSizeX()));
		tempVar.setY((long)(y / getCubeSizeY()));
		tempVar.setZ((long)(z / getCubeSizeZ()));
		return tempVar;
	}
	public CubeIndexMap(int cubeSizeX, int cubeSizeY, int cubeSizeZ) {
		if (cubeSizeX <= 0) {
			throw new IllegalArgumentException("cubeSizeX <= 0");
		}
		if (cubeSizeY <= 0) {
			throw new IllegalArgumentException("cubeSizeY <= 0");
		}
		if (cubeSizeZ <= 0) {
			throw new IllegalArgumentException("cubeSizeZ <= 0");
		}

		this.CubeSizeX = cubeSizeX;
		this.CubeSizeY = cubeSizeY;
		this.CubeSizeZ = cubeSizeZ;
	}

	/** 
	 perfrom action if cube exist.
	 under lock (cube)
	*/
	public final void TryPerfrom(CubeIndex index, tangible.Action2Param<CubeIndex, TCube> action) {
		TValue cube;
		tangible.OutObject<TValue> tempOut_cube = new tangible.OutObject<TValue>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (Cubes.TryGetValue(index, tempOut_cube)) {
		cube = tempOut_cube.outArgValue;
			synchronized (cube) {
				if (cube.State != Cube<TObject>.StateRemoved) {
					action.invoke(index, cube);
				}
			}
		}
	else {
		cube = tempOut_cube.outArgValue;
	}
	}

	/** 
	 perfrom action for Cubes.GetOrAdd.
	 under lock (cube)
	*/
	public final void Perform(CubeIndex index, tangible.Action2Param<CubeIndex, TCube> action) {
		while (true) {
			var cube = Cubes.putIfAbsent(index, (_) -> new TCube());
			synchronized (cube) {
				if (cube.State == Cube<TObject>.StateRemoved) {
					continue;
				}
				action.invoke(index, cube);
			}
		}
	}

	/** 
	 角色进入地图时
	*/
	public final void OnEnter(TObject obj, double x, double y, double z) {
		Perform(ToIndex(x, y, z), (index, cube) -> cube.Add(index, obj));
	}

	public final void OnEnter(TObject obj, float x, float y, float z) {
		Perform(ToIndex(x, y, z), (index, cube) -> cube.Add(index, obj));
	}

	public final void OnEnter(TObject obj, long x, long y, long z) {
		Perform(ToIndex(x, y, z), (index, cube) -> cube.Add(index, obj));
	}

	public final void OnEnter(TObject obj, CubeIndex index) {
		Perform(index, (index, cube) -> cube.Add(index, obj));
	}
	private void RemoveObject(CubeIndex index, TCube cube, TObject obj) {
		if (cube.Remove(index, obj)) {
			cube.State = Cube<TObject>.StateRemoved;
			TValue _;
			tangible.OutObject<TCube> tempOut__ = new tangible.OutObject<TCube>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			Cubes.TryRemove(index, tempOut__);
		_ = tempOut__.outArgValue;
		}
	}

	private boolean OnMove(CubeIndex oIndex, CubeIndex nIndex, TObject obj) {
		if (oIndex.equals(nIndex)) {
			return false;
		}

		TryPerfrom(oIndex, (index, cube) -> RemoveObject(index, cube, obj));
		Perform(nIndex, (index, cube) -> cube.Add(index, obj));
		return true;
	}

	/** 
	 角色位置变化时，
	 return true 如果cube发生了变化。
	 return false 还在原来的cube中。
	*/
	public final boolean OnMove(TObject obj, double oldx, double oldy, double oldz, double newx, double newy, double newz) {
		return OnMove(ToIndex(oldx, oldy, oldz), ToIndex(newx, newy, newz), obj);
	}

	public final boolean OnMove(TObject obj, float oldx, float oldy, float oldz, float newx, float newy, float newz) {
		return OnMove(ToIndex(oldx, oldy, oldz), ToIndex(newx, newy, newz), obj);
	}

	public final boolean OnMove(TObject obj, long oldx, long oldy, long oldz, long newx, long newy, long newz) {
		return OnMove(ToIndex(oldx, oldy, oldz), ToIndex(newx, newy, newz), obj);
	}

	public final boolean OnMove(TObject obj, CubeIndex oldIndex, CubeIndex newIndex) {
		return OnMove(oldIndex, newIndex, obj);
	}
	/** 
	 角色离开地图时
	*/
	public final void OnLeave(TObject obj, double x, double y, double z) {
		TryPerfrom(ToIndex(x, y, z), (index, cube) -> RemoveObject(index, cube, obj));
	}

	public final void OnLeave(TObject obj, float x, float y, float z) {
		TryPerfrom(ToIndex(x, y, z), (index, cube) -> RemoveObject(index, cube, obj));
	}

	public final void OnLeave(TObject obj, long x, long y, long z) {
		TryPerfrom(ToIndex(x, y, z), (index, cube) -> RemoveObject(index, cube, obj));
	}

	public final void OnLeave(TObject obj, CubeIndex index) {
		TryPerfrom(index, (index, cube) -> RemoveObject(index, cube, obj));
	}

	public final ArrayList<TCube> GetCubes(CubeIndex center, int rangeX, int rangeY, int rangeZ) {
		ArrayList<TCube> result = new ArrayList<TCube>();
		for (long i = center.getX() - rangeX; i <= center.getX() + rangeX; ++i) {
			for (long j = center.getY() - rangeY; j <= center.getY() + rangeY; ++j) {
				for (long k = center.getZ() - rangeZ; k <= center.getZ() + rangeZ; ++k) {
					var index = new CubeIndex();
					index.setX(i);
					index.setY(j);
					index.setZ(k);
					TValue cube;
					tangible.OutObject<TValue> tempOut_cube = new tangible.OutObject<TValue>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
					if (Cubes.TryGetValue(index, tempOut_cube)) {
					cube = tempOut_cube.outArgValue;
						result.add(cube);
					}
				else {
					cube = tempOut_cube.outArgValue;
				}
				}
			}
		}
		return result;
	}

	/** 
	 返回 center 坐标所在的 cube 周围的所有 cube。
	 可以遍历返回的Cube的所有角色，进一步进行精确的距离判断。
	*/

	public final java.util.ArrayList<TCube> GetCubes(double centerX, double centerY, double centerZ, int rangeX, int rangeY) {
		return GetCubes(centerX, centerY, centerZ, rangeX, rangeY, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(double centerX, double centerY, double centerZ, int rangeX) {
		return GetCubes(centerX, centerY, centerZ, rangeX, 4, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(double centerX, double centerY, double centerZ) {
		return GetCubes(centerX, centerY, centerZ, 4, 4, 4);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<TCube> GetCubes(double centerX, double centerY, double centerZ, int rangeX = 4, int rangeY = 4, int rangeZ = 4)
	public final ArrayList<TCube> GetCubes(double centerX, double centerY, double centerZ, int rangeX, int rangeY, int rangeZ) {
		return GetCubes(ToIndex(centerX, centerY, centerZ), rangeX, rangeY, rangeZ);
	}


	public final java.util.ArrayList<TCube> GetCubes(float centerX, float centerY, float centerZ, int rangeX, int rangeY) {
		return GetCubes(centerX, centerY, centerZ, rangeX, rangeY, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(float centerX, float centerY, float centerZ, int rangeX) {
		return GetCubes(centerX, centerY, centerZ, rangeX, 4, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(float centerX, float centerY, float centerZ) {
		return GetCubes(centerX, centerY, centerZ, 4, 4, 4);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<TCube> GetCubes(float centerX, float centerY, float centerZ, int rangeX = 4, int rangeY = 4, int rangeZ = 4)
	public final ArrayList<TCube> GetCubes(float centerX, float centerY, float centerZ, int rangeX, int rangeY, int rangeZ) {
		return GetCubes(ToIndex(centerX, centerY, centerZ), rangeX, rangeY, rangeZ);
	}


	public final java.util.ArrayList<TCube> GetCubes(long centerX, long centerY, long centerZ, int rangeX, int rangeY) {
		return GetCubes(centerX, centerY, centerZ, rangeX, rangeY, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(long centerX, long centerY, long centerZ, int rangeX) {
		return GetCubes(centerX, centerY, centerZ, rangeX, 4, 4);
	}

	public final java.util.ArrayList<TCube> GetCubes(long centerX, long centerY, long centerZ) {
		return GetCubes(centerX, centerY, centerZ, 4, 4, 4);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<TCube> GetCubes(long centerX, long centerY, long centerZ, int rangeX = 4, int rangeY = 4, int rangeZ = 4)
	public final ArrayList<TCube> GetCubes(long centerX, long centerY, long centerZ, int rangeX, int rangeY, int rangeZ) {
		return GetCubes(ToIndex(centerX, centerY, centerZ), rangeX, rangeY, rangeZ);
	}
}