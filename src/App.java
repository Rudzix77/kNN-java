import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class App {

	private static final String trainSet = "iris.data.normalized.txt";
	private static final String testSet = "iris.data.normalized.test.txt";

	public static void main(String[] args){

		try(Scanner in = new Scanner(System.in)){
			System.out.println("Provide K value");

			int K = Integer.parseInt(in.nextLine());

			Trainer t = new Trainer(Paths.get(trainSet), Paths.get(testSet), K);
				t.loadData();

			System.out.println("Accurancy: " + t.test());

			System.out.println("You can now type vectors to predict or 'graph' to draw graph of k-accurancy relation");

			while(in.hasNextLine()){
				String line = in.nextLine();

				if(!line.equals("graph")){
					System.out.println(t.predict(new Point(Arrays.stream(line.split("[\\s,]")).map(Double::valueOf).toArray(Double[]::new))));
				}else{
					System.out.println("Provide limit of the K value to check");
					Trainer.drawGraph(t, in.nextInt());
				}
			}

		}catch (NumberFormatException ex){
			System.err.println("The K value must be positive integer");
		}catch (IndexOutOfBoundsException ex){
			System.err.println("Bad argument. Coordinates amount is wrong");
		} catch (IOException e) {
			System.err.println("Problem with loading trainingSet or testSet files.");
		}
	}
}

class Trainer{

	private int K;

	private Path trainPath;
	private List<Point> trainSet;

	private Path testPath;
	private List<Point> testSet;


	Trainer(Path trainSet, Path testSet, int K){
		this.trainPath = trainSet;
		this.testPath = testSet;
		this.K = K;
	}

	void loadData() throws IOException{
		trainSet = loadFromFile(trainPath);
		testSet = loadFromFile(testPath);
	}

	private List<Point> loadFromFile(Path p) throws IOException{

		List<Point> points = new ArrayList<>();

		Files.lines(p).forEach(l -> {
			String[] data = l.split(",");

			Point point = new Point(data[data.length - 1]);

			Stream.of(data).limit(data.length - 1).forEach(e -> point.addCord(Double.valueOf(e)));

			points.add(point);
		});

		return points;
	}

	double test(){
		List<Boolean> classifications = testSet.stream().map(e -> predict(e).equals(e.name)).collect(Collectors.toList());

		return (double) Collections.frequency(classifications, true) / testSet.size();
	}

	String predict(Point p){

		TreeMap<Double, Point> distances = new TreeMap<>();

		trainSet.forEach(t -> distances.put(p.getDistance(t), t));

		return distances.values().stream().limit(K).collect(Collectors.groupingBy(o -> o, Collectors.counting()))
				.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey().name;
	}

	static void drawGraph(Trainer t, int limit){
		try{

			for(int n = 1; n < limit + 1; n++){
				Trainer tester = new Trainer(t.trainPath, t.testPath, n);
				tester.loadData();

				double result = tester.test();

				System.out.println("K = " + n);

				for(int g = 0; g < Math.round((result * 100)/3); g++){
					System.out.print("⬜️️");
				}

				System.out.printf("%.2f%%%n", result * 100);
			}

		}catch (IOException e){
			System.err.println("Problem occurred during graph drawing");
		}
	}
}

class Point{
	String name;
	List<Double> cords = new ArrayList<>();

	Point(String name){
		this.name = name;
	}

	Point(Double[] cords){
		this.name = "Unknown";
		this.cords = Arrays.asList(cords);
	}

	void addCord(Double cord){
		cords.add(cord);
	}

	double getDistance(Point p){
		double sum = IntStream.range(0, cords.size()).mapToDouble(n -> Math.pow(cords.get(n) - p.cords.get(n), 2)).sum();

		return Math.sqrt(sum);
	}

	public String toString(){
		return String.format("%s: %s", name, cords);
	}
}
