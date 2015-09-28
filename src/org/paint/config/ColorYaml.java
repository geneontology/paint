package org.paint.config;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;


public class ColorYaml {

	public float[] values;
	public Color[] colors;
	
	public static void main(String[] args) {
		Yaml yaml = createYaml();
		ColorYaml inst = new ColorYaml();
		inst.colors = new Color[] {
			new Color(1,1,1,10),
			new Color(2,2,2,0)
		};
		inst.values = new float[] {
			0.0f, 0.1f, 0.4f	
		};
		String dump = yaml.dump(inst);
		System.out.println(dump);
		
		Object load = yaml.load(dump);
		
		String dump2 = yaml.dump(load);
		System.out.println(dump2);
	}
	
	public static Yaml createYaml() {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumperOptions.setIndent(2);
		Representer representer = new ColorRepresenter();
		Constructor constructor = new ColorConstructor();
		Yaml yaml = new Yaml(constructor, representer, dumperOptions);
		return yaml;
	}
	
	static class ColorRepresenter extends Representer {
		
		public ColorRepresenter() {
			this.representers.put(Color.class, new RepresentColor());
		}

		private final class RepresentColor implements Represent {
			@Override
			public Node representData(Object data) {
				Color c = (Color) data;
				StringBuilder sb = new StringBuilder();
				sb.append(c.getRed()).append(", ").append(c.getGreen()).append(", ").append(c.getBlue()).append(", ").append(c.getAlpha());
				return representScalar(new Tag("!color"), sb.toString());
			}
		}
	}
	
	static class ColorConstructor extends Constructor {
		
		public ColorConstructor() {
			super(ColorYaml.class);
			this.yamlConstructors.put(new Tag("!color"), new ConstructColor());
		}
		
		class ConstructColor extends AbstractConstruct {

			@Override
			public Object construct(Node node) {
				String val = (String) constructScalar((ScalarNode) node);
				String[] split = StringUtils.split(val, ',');
				if (split.length == 4 || split.length == 3) {
					int[] values = new int[split.length];
						boolean hasProblems = false;
					for (int i = 0; i < split.length; i++) {
						String cleanSplit = StringUtils.trimToNull(split[i]);
						if (cleanSplit != null) {
							try {
								values[i] = Integer.parseInt(cleanSplit);
							} catch (NumberFormatException e) {
								hasProblems = true;
								break;
							}
						}
						else {
							hasProblems = true;
							break;
						}
					}
					if (hasProblems == false) {
						if (values.length == 4) {
							return new Color(values[0], values[1], values[2], values[3]);
						}
						else if (values.length == 3) {
							return new Color(values[0], values[1], values[2]);
						}
					}
				}
				return null;
			}
			
		}
	}
}
