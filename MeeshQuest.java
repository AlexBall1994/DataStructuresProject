package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	public static void main(String[] args) {

		Document results = null;
		TreeMap<Point2D.Float, City> coordsAndCities = new TreeMap<Point2D.Float, City>(
				new CityCoordinatesComparator());
		TreeMap<String, City> cityNames = new TreeMap<String, City>(new cityNamesComparator());
		List<String> isolatedCitiesMapped = new ArrayList<String>();
		ArrayList<String> citiesMapped = new ArrayList<String>();
		List<String> allCitiesMapped = new ArrayList<String>();
		ArrayList<Road> roadsMapped = new ArrayList<Road>();
		ArrayList<Road> roadsOriginalMapped = new ArrayList<Road>();
		List<String> citiesMappedToAvl = new ArrayList<String>();
		CanvasPlus canvas;

		try {

			Document doc = XmlUtility.validateNoNamespace(System.in);
			results = XmlUtility.getDocumentBuilder().newDocument();
			Element result = results.createElement("results");
			results.appendChild(result);


			Element commandNode = doc.getDocumentElement();

			float spatialWidth = Float.parseFloat(commandNode.getAttribute("spatialWidth"));
			float spatialHeight = Float.parseFloat(commandNode.getAttribute("spatialHeight"));
			int g = Integer.parseInt(commandNode.getAttribute("g"));

			double width = Double.parseDouble(commandNode.getAttribute("spatialWidth"));
			double height = Double.parseDouble(commandNode.getAttribute("spatialHeight"));
			int order = Integer.parseInt(commandNode.getAttribute("pmOrder"));

			PMQuadTree PMQTree = new PMQuadTree(order, width, height);



			canvas = new CanvasPlus("MeeshQuest", (int) spatialWidth, (int) spatialHeight);
			canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK, false);
			QuadTree prQTree = new QuadTree(spatialWidth, spatialHeight, null, canvas);
			AvlGTree tree = new AvlGTree(g);

			final NodeList nl = commandNode.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
					commandNode = (Element) nl.item(i);
					Element success = results.createElement("success");
					Element command = results.createElement("command");
					Element parameters = results.createElement("parameters");
					Element output = results.createElement("output");
					Element error = results.createElement("error");
					String idV = commandNode.getAttribute("id");

					if (!idV.equals(""))command.setAttribute("id", idV);

					if (commandNode.getNodeName().equals("createCity")) {

						String name = commandNode.getAttribute("name");
						String color = commandNode.getAttribute("color");
						float xCord = Float.parseFloat(commandNode.getAttribute("x"));
						float yCord = Float.parseFloat(commandNode.getAttribute("y"));
						float radi = Float.parseFloat(commandNode.getAttribute("radius"));
						Point2D.Float point = new Point2D.Float(xCord, yCord);

						Element cityName = results.createElement("name");
						Element cityXPos = results.createElement("x");
						Element cityYPos = results.createElement("y");
						Element cityRadius = results.createElement("radius");
						Element cityColor = results.createElement("color");

						cityName.setAttribute("value", name);
						cityXPos.setAttribute("value", commandNode.getAttribute("x"));
						cityYPos.setAttribute("value", commandNode.getAttribute("y"));
						cityRadius.setAttribute("value", commandNode.getAttribute("radius"));
						cityColor.setAttribute("value", color);
						command.setAttribute("name", "createCity");

						parameters.appendChild(cityName);
						parameters.appendChild(cityXPos);
						parameters.appendChild(cityYPos);
						parameters.appendChild(cityRadius);
						parameters.appendChild(cityColor);


						City city = new City(name, xCord, yCord, color, radi);

						if (cityNames.containsKey(name)) {

							error.setAttribute("type", "duplicateCityName");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						} else if (coordsAndCities.containsKey(point)) {

							error.setAttribute("type", "duplicateCityCoordinates");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						} 
						else {
							boolean inRange = false;
							Utilities u = new Utilities();

							for(Map.Entry<Point2D.Float, City> entry: coordsAndCities.entrySet()){
								float diff = (float) u.distance(city.getX(), city.getY(),entry.getValue().getX(), entry.getValue().getY());
								if (diff < entry.getValue().getRadius()) inRange = true;

							}

							if (inRange){
								error.setAttribute("type", "cityInRangeOfOtherCity");

								result.appendChild(error);
								result.appendChild(command);
								result.appendChild(parameters);

							}
							else {
								cityNames.put(name, city);
								coordsAndCities.put(point, city);
								tree.insert(city);

								result.appendChild(success);
								success.appendChild(command);
								success.appendChild(parameters);
								success.appendChild(output);
							}
						}
						// canvas.draw();
					}
					else if (commandNode.getNodeName().equals("mapCity")){
						String name = commandNode.getAttribute("name");

						Element nameP = results.createElement("name");

						command.setAttribute("name", "mapCity");
						nameP.setAttribute("value", name);

						parameters.appendChild(nameP);

						if (cityNames.containsKey(name)){
							City city = cityNames.get(name);
							if (!citiesMappedToAvl.contains(name)){

								citiesMappedToAvl.add(name);
							}
							tree.insert(city);


							if (allCitiesMapped.contains(name)){
								error.setAttribute("type", "cityAlreadyMapped");

								result.appendChild(error);
								error.appendChild(command);
								error.appendChild(parameters);
							}
							else {

								if ((city.getX() < 0.0) || (city.getX() > (double) spatialWidth) || (city.getY() < 0.0) || (city.getY() > (double) spatialHeight)){
									error.setAttribute("type", "cityOutOfBounds");

									result.appendChild(error);
									error.appendChild(command);
									error.appendChild(parameters);
								}
								else {


									result.appendChild(success);
									success.appendChild(command);
									success.appendChild(parameters);
									parameters.appendChild(nameP);
									success.appendChild(output);

									isolatedCitiesMapped.add(name);
									allCitiesMapped.add(name);
									city.isolate();
									PMQTree.insert(city);
									canvas.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
									for(int j = 0; j < roadsMapped.size(); j++)
										PMQTree.insertRoad(roadsMapped.get(j));
									//	prQTree.insert(city);	
								}
							}
						}
						else {
							error.setAttribute("type", "nameNotInDictionary");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						}
					}

					else if(commandNode.getNodeName().equals("printPMQuadtree")){
						if (PMQTree.isEmpty()){
							error.setAttribute("type", "mapIsEmpty");
							command.setAttribute("name", "printPMQuadtree");
							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						}
						else {
							for(int j = 0; j < roadsMapped.size(); j++)
								PMQTree.insertRoad(roadsMapped.get(j));
							PMQTree.print(results, result);
						}
						//prQTree.print(results, result);
					}
					else if(commandNode.getNodeName().equals("rangeRoads")){
						double x = Double.parseDouble(commandNode.getAttribute("x"));
						double y = Double.parseDouble(commandNode.getAttribute("y"));
						double radius = Double.parseDouble(commandNode.getAttribute("radius"));
						Point2D.Double p = new Point2D.Double(x, y);

						String saveMap = commandNode.getAttribute("saveMap");
						TreeMap<Road, Road> roadsInRangeList = new TreeMap<Road, Road>(new roadNamesComparator());


						Element roadList = results.createElement("roadList");


						Element xE = results.createElement("x");
						Element yE = results.createElement("y");
						Element radiusE = results.createElement("radius");
						Element mapName = results.createElement("saveMap");

						command.setAttribute("name", "rangeRoads");
						xE.setAttribute("value", Integer.toString((int) x));
						yE.setAttribute("value", Integer.toString((int) y));
						radiusE.setAttribute("value", Integer.toString((int) radius));

						parameters.appendChild(xE);
						parameters.appendChild(yE);
						parameters.appendChild(radiusE);
						canvas.addCircle(x, y, radius, Color.BLUE, false);

						for(int j = 0; j < roadsMapped.size(); j++){
							if (roadsMapped.get(j).getLine().ptSegDist(p) <= radius){
								roadsInRangeList.put(roadsMapped.get(j), roadsMapped.get(j));
							}
						}

						if (!saveMap.equals("")){
							mapName.setAttribute("value", saveMap);
							parameters.appendChild(mapName);
							canvas.save(saveMap);
							canvas.addCircle(x, y, radius, Color.BLUE, false);

						}

						if (roadsInRangeList.isEmpty()){
							error.setAttribute("type", "noRoadsExistInRange");
							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);

						}
						else {
							for (Road entry: roadsInRangeList.keySet()){
								Road r = roadsInRangeList.get(entry);
								Element road = results.createElement("road");
								road.setAttribute("start", r.getStart());
								road.setAttribute("end", r.getEnd());

								roadList.appendChild(road);

							}

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(roadList);

						}			
					}
					else if (commandNode.getNodeName().equals("rangeCities")){
						double x = Double.parseDouble(commandNode.getAttribute("x"));
						double y = Double.parseDouble(commandNode.getAttribute("y"));
						double radius = Double.parseDouble(commandNode.getAttribute("radius"));
						Point2D.Double p = new Point2D.Double(x, y);

						String saveMap = commandNode.getAttribute("saveMap");
						TreeMap<String, City> cityInRangeList = new TreeMap<String, City>(new cityNamesComparator());


						Element cityList = results.createElement("cityList");


						Element xE = results.createElement("x");
						Element yE = results.createElement("y");
						Element radiusE = results.createElement("radius");
						Element mapName = results.createElement("saveMap");

						command.setAttribute("name", "rangeCities");
						xE.setAttribute("value", Integer.toString((int) x));
						yE.setAttribute("value", Integer.toString((int) y));
						radiusE.setAttribute("value", Integer.toString((int) radius));
						error.setAttribute("type", "noCitiesExistInRange");

						parameters.appendChild(xE);
						parameters.appendChild(yE);
						parameters.appendChild(radiusE);
						canvas.addCircle(x, y, radius, Color.BLUE, false);

						for(int j = 0; j < allCitiesMapped.size(); j++){
							String current = allCitiesMapped.get(j);
							City c = cityNames.get(current);
							if(p.distance(c) <= radius){
								cityInRangeList.put(current, c);
							}				
						}


						if (!saveMap.equals("")){
							mapName.setAttribute("value", saveMap);
							parameters.appendChild(mapName);
							canvas.save(saveMap);
							canvas.addCircle(x, y, radius, Color.BLUE, false);

						}


						if (cityInRangeList.isEmpty()){
							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);

						}
						else {
							for (String entry: cityInRangeList.keySet()){
								City c = cityInRangeList.get(entry);
								Element city = results.createElement("city");
								city.setAttribute("name", c.getName());
								city.setAttribute("x", Integer.toString((int)c.getX()));
								city.setAttribute("y", Integer.toString((int)c.getY()));
								city.setAttribute("color", c.getColor());
								city.setAttribute("radius", Integer.toString((int)c.getRadius()));
								cityList.appendChild(city);

							}

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(cityList);
						}
					}
					else if (commandNode.getNodeName().equals("listCities")){

						if (coordsAndCities.isEmpty()){

							error.setAttribute("type", "noCitiesToList");
							command.setAttribute("name", "listCities");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						}
						else {

							Element sortBy = results.createElement("sortBy");					
							Element cityList = results.createElement("cityList");

							sortBy.setAttribute("value", commandNode.getAttribute("sortBy"));
							command.setAttribute("name", "listCities");

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(cityList);
							parameters.appendChild(sortBy);

							if (commandNode.getAttribute("sortBy").equals("name")){

								for(Map.Entry<String,City> entry : cityNames.entrySet()){
									City c = entry.getValue();
									Element city = results.createElement("city");

									int xVal = (int) c.getX();
									int yVal = (int) c.getY();
									int radi = (int) c.getRadius();
									String color = c.getColor();
									String name = c.getName();
									String radius = Integer.toString(radi);		 
									String x = Integer.toString(xVal);
									String y = Integer.toString(yVal);


									city.setAttribute("name", name);
									city.setAttribute("x", x);
									city.setAttribute("y", y);
									city.setAttribute("color", color);				
									city.setAttribute("radius", radius);



									cityList.appendChild(city);
								}
							}
							else {
								for(Map.Entry<Point2D.Float, City> entry : coordsAndCities.entrySet()){
									City c = entry.getValue();
									Element city = results.createElement("city");

									int xVal = (int) c.getX();
									int yVal = (int) c.getY();
									int radi = (int) c.getRadius();
									String color = c.getColor();
									String name = c.getName();
									String radius = Integer.toString(radi);		 
									String x = Integer.toString(xVal);
									String y = Integer.toString(yVal);

									city.setAttribute("name", name);
									city.setAttribute("x", x);
									city.setAttribute("y", y);
									city.setAttribute("color", color);
									city.setAttribute("radius", radius);


									cityList.appendChild(city);

								}
							}
						}
					}
					else if (commandNode.getNodeName().equals("unmapCity")){
						/*String name = commandNode.getAttribute("name");

						Element nameP = results.createElement("name");

						nameP.setAttribute("value", name);
						command.setAttribute("name", "unmapCity");

						if (citiesMapped.contains(name)){

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							parameters.appendChild(nameP);
							success.appendChild(output);

							City c = cityNames.get(name);


							prQTree.delete(c);
							citiesMapped.remove(c.getName());

						}
						else if (!cityNames.containsKey(name)){
							error.setAttribute("type", "nameNotInDictionary");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
							parameters.appendChild(nameP);
						}
						else {
							error.setAttribute("type", "cityNotMapped");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
							parameters.appendChild(nameP);
						}*/
					}

					else if (commandNode.getNodeName().equals("deleteCity")){
						/*
						String name = commandNode.getAttribute("name");

						Element nameP = results.createElement("name");

						command.setAttribute("name", "deleteCity");
						nameP.setAttribute("value", name);

						if (!cityNames.containsKey(name)){

							Element deleteError = results.createElement("error");


							deleteError.setAttribute("type", "cityDoesNotExist");


							result.appendChild(deleteError);
							deleteError.appendChild(command);
							deleteError.appendChild(parameters);
							parameters.appendChild(nameP);


						} 
						else {
							City c = cityNames.get(name);
							Point2D.Float point = new Point2D.Float((float)c.getX(), (float)c.getY());
							cityNames.remove(c.getName());
							coordsAndCities.remove(point);


							if (citiesMapped.contains(name)){
								prQTree.delete(c);
								citiesMapped.remove(c.getName());
								Element cityUnmapped = results.createElement("cityUnmapped");
								cityUnmapped.setAttribute("name", name);
								cityUnmapped.setAttribute("x", Integer.toString((int) c.getX()));
								cityUnmapped.setAttribute("y", Integer.toString((int) c.getY()));
								cityUnmapped.setAttribute("color", c.getColor());
								cityUnmapped.setAttribute("radius", Integer.toString((int)c.getRadius()));
								output.appendChild(cityUnmapped);
							}



							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							parameters.appendChild(nameP);
							success.appendChild(output);


						}*/
					}
					else if (commandNode.getNodeName().equals("clearAll")){
						prQTree = new QuadTree(spatialWidth, spatialHeight, null, canvas);
						PMQTree = new PMQuadTree(order, width, height);
						tree = new AvlGTree(g);
						cityNames = new TreeMap<String, City>(new cityNamesComparator());
						coordsAndCities = new TreeMap<Point2D.Float, City>(
								new CityCoordinatesComparator());
						isolatedCitiesMapped = new ArrayList<String>();
						citiesMappedToAvl = new ArrayList<String>();
						allCitiesMapped = new ArrayList<String>();
						canvas = new CanvasPlus("MeeshQuest", (int) spatialWidth, (int) spatialHeight);
						citiesMapped = new ArrayList<String>();
						roadsMapped = new ArrayList<Road>();
						roadsOriginalMapped = new ArrayList<Road>();

						command.setAttribute("name", "clearAll");

						result.appendChild(success);
						success.appendChild(command);
						success.appendChild(parameters);
						success.appendChild(output);
					}

					else if (commandNode.getNodeName().equals("printAvlTree")){


						command.setAttribute("name", "printAvlTree");

						if (tree.isEmpty()){
							error.setAttribute("type", "emptyTree");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);
						}
						else {

							Element avlGTree = results.createElement("AvlGTree");

							avlGTree.setAttribute("cardinality", Integer.toString(tree.size()));
							avlGTree.setAttribute("height", Integer.toString(tree.getHeight()));
							avlGTree.setAttribute("maxImbalance", Integer.toString(g));

							tree.print(results, avlGTree);

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(avlGTree);

						}
					}
					else if (commandNode.getNodeName().equals("saveMap")){

						Element nameE = results.createElement("name");
						String name = commandNode.getAttribute("name");


						command.setAttribute("name", "saveMap");
						nameE.setAttribute("value", name);

						result.appendChild(success);
						success.appendChild(command);
						success.appendChild(parameters);
						parameters.appendChild(nameE);
						success.appendChild(output);
						canvas.save(name);

					}
					else if (commandNode.getNodeName().equals("nearestRoad")){
						command.setAttribute("name", "nearestRoad");
						String xVal = commandNode.getAttribute("x");
						String yVal = commandNode.getAttribute("y");
						Element x = results.createElement("x");
						Element y = results.createElement("y");
						x.setAttribute("value", xVal);
						y.setAttribute("value", yVal);
						Point2D.Double p = new Point2D.Double(Double.parseDouble(xVal),Double.parseDouble(yVal));

						parameters.appendChild(x);
						parameters.appendChild(y);

						if(roadsMapped.isEmpty()){
							error.setAttribute("type", "roadNotFound");

							result.appendChild(error);
							error.appendChild(command);
							error.appendChild(parameters);

						}
						else {

							double closest = Double.MAX_VALUE;
							int index = 0;
							for(int k = 0; k < roadsMapped.size(); k++){

								if(roadsMapped.get(k).getLine().ptSegDist(p) < closest){
									index = k;
									closest = roadsMapped.get(k).getLine().ptSegDist(p);
								}					
								else if (roadsMapped.get(k).getLine().ptSegDist(p) == closest){
									if(roadsMapped.get(k).getStart().compareTo(roadsMapped.get(index).getStart()) > 0){
										index = k;
									}
									else if (roadsMapped.get(k).getStart().compareTo(roadsMapped.get(index).getStart()) == 0){
										if(roadsMapped.get(k).getEnd().compareTo(roadsMapped.get(index).getEnd()) > 0){
											index = k;
										}
									}
								}
							}
							Element road = results.createElement("road");

							if (roadsMapped.get(index).getStart().compareTo(roadsMapped.get(index).getEnd()) < 0){
								road.setAttribute("start", roadsMapped.get(index).getStart());
								road.setAttribute("end", roadsMapped.get(index).getEnd());
							}
							else {
								road.setAttribute("start", roadsMapped.get(index).getEnd());
								road.setAttribute("end", roadsMapped.get(index).getStart());
							}
							output.appendChild(road);
							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);



						}

					}
					else if (commandNode.getNodeName().equals("mapRoad")){
						String c1 = commandNode.getAttribute("start");
						String c2 = commandNode.getAttribute("end");



						Element city1 = results.createElement("start");
						Element city2 = results.createElement("end");

						city1.setAttribute("value", c1);
						city2.setAttribute("value", c2);

						parameters.appendChild(city1);
						parameters.appendChild(city2);
						error.appendChild(command);
						error.appendChild(parameters);


						command.setAttribute("name", "mapRoad");


						if (!cityNames.containsKey(c1)){
							error.setAttribute("type", "startPointDoesNotExist");
							result.appendChild(error);

						}
						else if (!cityNames.containsKey(c2)){
							error.setAttribute("type", "endPointDoesNotExist");
							result.appendChild(error);

						}
						else if (c1.equals(c2)){
							error.setAttribute("type", "startEqualsEnd");
							result.appendChild(error);

						}
						else if (isolatedCitiesMapped.contains(c1) || isolatedCitiesMapped.contains(c2)){
							error.setAttribute("type", "startOrEndIsIsolated");
							result.appendChild(error);



						}

						else {
							City city = cityNames.get(c1);
							City cityDos = cityNames.get(c2);
							Line2D.Double l = new Line2D.Double(city.getX(), city.getY(), cityDos.getX(), cityDos.getY());
							//I don't check the reverse of roads, potential error
							Road r;
							Road o = new Road(c1, c2, l);

							if (c1.compareTo(c2) < 0){
								r = new Road(c1,c2,l);
							}
							else {
								r = new Road(c2,c1,l);
							}



							if (r.alreadyMapped(roadsMapped, r)){
								error.setAttribute("type", "roadAlreadyMapped");
								result.appendChild(error);

							}
							else {				

								if ((city.getX() < 0.0) || (city.getX() > (double) spatialWidth) || (city.getY() < 0.0) || (city.getY() > (double) spatialHeight)){
									error.setAttribute("type", "roadOutOfBounds");
									result.appendChild(error);
								}
								else if((cityDos.getX() < 0.0) || (cityDos.getX() > (double) spatialWidth) || (cityDos.getY() < 0.0) || (cityDos.getY() > (double) spatialHeight)){
									error.setAttribute("type", "roadOutOfBounds");
									result.appendChild(error);
								}
								else {
									if (!allCitiesMapped.contains(c1)){
										citiesMapped.add(c1);
										allCitiesMapped.add(c1);
										PMQTree.insert(city);
									}
									if(!allCitiesMapped.contains(c2)){
										citiesMapped.add(c2);
										allCitiesMapped.add(c2);
										PMQTree.insert(cityDos);
									}

									tree.insert(city);
									tree.insert(cityDos);
									roadsOriginalMapped.add(o);
									roadsMapped.add(r);

									for(int j = 0; j < roadsMapped.size(); j++)
										PMQTree.insertRoad(roadsMapped.get(j));

									Element roadCreated = results.createElement("roadCreated");
									roadCreated.setAttribute("start", c1);
									roadCreated.setAttribute("end", c2);

									output.appendChild(roadCreated);
									result.appendChild(success);
									success.appendChild(command);
									success.appendChild(parameters);
									success.appendChild(output);
								}
							}
						}
					}
					else if (commandNode.getNodeName().equals("nearestIsolatedCity")){
						String xVal = commandNode.getAttribute("x");
						String yVal = commandNode.getAttribute("y");

						Element xE = results.createElement("x");
						Element yE = results.createElement("y");

						xE.setAttribute("value", xVal);
						yE.setAttribute("value", yVal);

						parameters.appendChild(xE);
						parameters.appendChild(yE);

						command.setAttribute("name", "nearestIsolatedCity");


						if(isolatedCitiesMapped.isEmpty()){
							error.setAttribute("type", "cityNotFound");
							error.appendChild(command);
							error.appendChild(parameters);

							result.appendChild(error);

						}
						else{
							Utilities u = new Utilities();
							double xD = Double.parseDouble(xVal);
							double yD = Double.parseDouble(yVal);
							String current = isolatedCitiesMapped.get(0);
							String bestCity = current;
							City c = cityNames.get(current);
							double best = u.distance(c.getX(), c.getY(), xD, yD);
							Element city = results.createElement("city");

							for(int j = 0; j < isolatedCitiesMapped.size(); i++){

								current = isolatedCitiesMapped.get(j);
								c = cityNames.get(current);		
								if (u.distance(c.getX(), c.getY(), xD, yD) < best){
									bestCity = current;
									best = u.distance(c.getX(), c.getY(), xD, yD);

								}
								else if (u.distance(c.getX(), c.getY(), xD, yD) == best){
									if (current.compareTo(bestCity) > 0)
										bestCity = current;
								}	
							}	
							c = cityNames.get(bestCity);

							city.setAttribute("name", c.getName());
							city.setAttribute("x", Integer.toString((int) c.getX()));
							city.setAttribute("y", Integer.toString((int) c.getY()));
							city.setAttribute("color", c.getColor());
							city.setAttribute("radius", Integer.toString((int) c.getRadius()));


							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(city);
						}
					}
					else if (commandNode.getNodeName().equals("nearestCityToRoad")){
						String c1 = commandNode.getAttribute("start");
						String c2= commandNode.getAttribute("end");
						City city = cityNames.get(c1);
						City cityDos = cityNames.get(c2);
						Line2D.Double l = new Line2D.Double(city.getX(), city.getY(), cityDos.getX(), cityDos.getY());
						//I don't check the reverse of roads, potential error
						Road r;

						Element start = results.createElement("start");
						Element end = results.createElement("end");
						start.setAttribute("value", c1);
						end.setAttribute("value", c2);
						parameters.appendChild(start);
						parameters.appendChild(end);


						command.setAttribute("name", "nearestCityToRoad");

						if (c1.compareTo(c2) < 0){
							r = new Road(c1,c2,l);
						}
						else {
							r = new Road(c2,c1,l);
						}


						if(!r.alreadyMapped(roadsMapped, r)){
							error.setAttribute("type", "roadIsNotMapped");
							error.appendChild(command);
							error.appendChild(parameters);
							result.appendChild(error);

						}
						else if(allCitiesMapped.size() == 2){
							error.setAttribute("type", "noOtherCitiesMapped");
							error.appendChild(command);
							error.appendChild(parameters);
							result.appendChild(error);

						}
						else {
							String current;
							City c;
							double bestDist = Double.MAX_VALUE;
							String best = allCitiesMapped.get(0);

							for(int j = 0 ; j < allCitiesMapped.size(); j++){
								current = allCitiesMapped.get(j);
								c = cityNames.get(current);
								if (!c.equals(city) && !c.equals(cityDos)){
									if(l.ptSegDist(c) < bestDist){
										bestDist = l.ptSegDist(c);
										best = current;

									}
									else if (l.ptSegDist(c) == bestDist){
										if (current.compareTo(best) > 0){
											best = current;

										}	
									}
								}
							}
							c = cityNames.get(best);

							Element cityE = results.createElement("city");

							cityE.setAttribute("name", c.getName());
							cityE.setAttribute("x", Integer.toString((int) c.getX()));
							cityE.setAttribute("y", Integer.toString((int) c.getY()));
							cityE.setAttribute("color", c.getColor());
							cityE.setAttribute("radius", Integer.toString((int) c.getRadius()));


							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(cityE);

						}
					}
					else if (commandNode.getNodeName().equals("shortestPath")){
						Road r = new Road(null,null,null);
						String start = commandNode.getAttribute("start");
						String end = commandNode.getAttribute("end");
						String HTML = commandNode.getAttribute("saveHTML");
						String saveMap = commandNode.getAttribute("saveMap");
						Element st = results.createElement("start");
						Element ed = results.createElement("end");


						st.setAttribute("value", start);
						ed.setAttribute("value", end);

						
						parameters.appendChild(st);
						parameters.appendChild(ed);
						if(!saveMap.equals("")){
							Element saveM = results.createElement("saveMap");
							saveM.setAttribute("value",saveMap);
							parameters.appendChild(saveM);

						}
						if(!HTML.equals("")){
							Element saveHTML = results.createElement("saveHTML");
							saveHTML.setAttribute("value", HTML);
							parameters.appendChild(saveHTML);
							
						}

						command.setAttribute("name", "shortestPath");

						if(!citiesMapped.contains(start)){
							error.setAttribute("type", "nonExistentStart");
							error.appendChild(command);
							error.appendChild(parameters);
							result.appendChild(error);
						}
						else if(!citiesMapped.contains(end)){
							error.setAttribute("type", "nonExistentEnd");
							error.appendChild(command);
							error.appendChild(parameters);
							result.appendChild(error);
						}
						else {



							ArrayList<Road> roadList = new ArrayList<Road>();
							double dist = 0;
							roadList = r.FOURTWENTYBlazeDjSoftStuff(citiesMapped, roadsOriginalMapped, start, end);

							if (roadList == null){
								error.setAttribute("type", "noPathExists");
								error.appendChild(command);
								error.appendChild(parameters);
								result.appendChild(error);
							}
							else {
								Element path = results.createElement("path");
							

								result.appendChild(success);
								success.appendChild(command);
								success.appendChild(parameters);
								success.appendChild(output);
							


								Point2D p;
								Point2D p2;


								path.setAttribute("hops", Integer.toString(roadList.size()));

								for(int j = roadList.size()-1; j >= 0; j--){
									Element road = results.createElement("road");
									if(roadList.get(j).getStart().equals(start)){
										road.setAttribute("start", roadList.get(j).getStart());
										road.setAttribute("end", roadList.get(j).getEnd());
										start = roadList.get(j).getEnd();
										p = roadList.get(j).getLine().getP1();
										p2 = roadList.get(j).getLine().getP2();
									}
									else {
										road.setAttribute("start", roadList.get(j).getEnd());
										road.setAttribute("end", roadList.get(j).getStart());
										start = roadList.get(j).getStart();
										p = roadList.get(j).getLine().getP2();
										p2 = roadList.get(j).getLine().getP1();
									}

									path.appendChild(road);

									Point2D p3;
									if(j-1 != -1){
										if(start.equals(roadList.get(j-1).getEnd())){
											p3 = roadList.get(j-1).getLine().getP1();
										}
										else {
											p3 = roadList.get(j-1).getLine().getP2();
										}
										Arc2D.Double arc = new Arc2D.Double();
										arc.setArcByTangent(p,p2,p3,1);


										Double angle = 180.0-arc.getAngleExtent();

										if(angle <= 135.0){
											Element right = results.createElement("right");
											path.appendChild(right);
										}
										else if (angle > 225.0){
											Element left = results.createElement("left");
											path.appendChild(left);
										}
										else {
											Element straight = results.createElement("straight");
											path.appendChild(straight);
										}
									}
									dist += p.distance(p2);

								}
								if(dist == 0.0){
									path.setAttribute("length", "0.000");
								}
								else {
									DecimalFormat df = new DecimalFormat("#.000");
									path.setAttribute("length", df.format(dist));
								}

								output.appendChild(path);
							}
						}
					}
					else if (commandNode.getNodeName().equals("nearestCity")){
						String xVal = commandNode.getAttribute("x");
						String yVal = commandNode.getAttribute("y");

						Element xE = results.createElement("x");
						Element yE = results.createElement("y");

						xE.setAttribute("value", xVal);
						yE.setAttribute("value", yVal);

						parameters.appendChild(xE);
						parameters.appendChild(yE);

						if(roadsMapped.isEmpty()){
							command.setAttribute("name", "nearestCity");
							error.setAttribute("type", "cityNotFound");
							error.appendChild(command);
							error.appendChild(parameters);

							result.appendChild(error);

						}
						else {
							Utilities u = new Utilities();
							double xD = Double.parseDouble(xVal);
							double yD = Double.parseDouble(yVal);

							String current = citiesMapped.get(0);
							String bestCity = current;
							City c = cityNames.get(current);
							double best = u.distance(c.getX(), c.getY(), xD, yD);
							Element city = results.createElement("city");

							for(int j = 0; j < citiesMapped.size(); j++){
								current = citiesMapped.get(j);
								c = cityNames.get(current);
								if (u.distance(c.getX(), c.getY(), xD, yD) < best){
									bestCity = current;
									best = u.distance(c.getX(), c.getY(), xD, yD);

								}
								else if (u.distance(c.getX(), c.getY(), xD, yD) == best){
									if (current.compareTo(bestCity) > 0)
										bestCity = current;
								}	
							}	
							c = cityNames.get(bestCity);

							city.setAttribute("name", c.getName());
							city.setAttribute("x", Integer.toString((int) c.getX()));
							city.setAttribute("y", Integer.toString((int) c.getY()));
							city.setAttribute("color", c.getColor());
							city.setAttribute("radius", Integer.toString((int) c.getRadius()));

							command.setAttribute("name", "nearestCity");

							result.appendChild(success);
							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							output.appendChild(city);


						}
					}
					else {
						Element undefined = results.createElement("undefinedError");
						result.appendChild(undefined);

					}
				}
			}

		} catch (SAXException | IOException | ParserConfigurationException e) {
			//System.out.println("Error:" + e);
			/* TODO: Process fatal error here */
			try {
				results = XmlUtility.getDocumentBuilder().newDocument();
				Element fatalError = results.createElement("fatalError");
				results.appendChild(fatalError);	
			}
			catch(Exception e1){

			}


		} finally {
			try {
				//	PrintStream out;
				//	try {
			//		out = new PrintStream(new FileOutputStream("outputMapRoad.xml"));
				//	System.setOut(out);
			//	} catch (FileNotFoundException e) {
				////TODO Auto-generated catch block
				//	e.printStackTrace();
				//	}

				XmlUtility.print(results);



			} catch (TransformerException e) {
				e.printStackTrace();
			} 
		}
	}
}
