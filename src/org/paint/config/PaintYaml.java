package org.paint.config;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

public class PaintYaml {

	private static Logger LOG = Logger.getLogger(PaintYaml.class);

	/**
	 * Constructor.
	 */
	public PaintYaml() {
		// Nobody here.
	}

	public static void main(String[] args) {
		PaintYaml inst = new PaintYaml();

		inst.loadConfig("config/preferences.yaml");
		
		PaintConfig config = PaintConfig.inst();
		String dump = config.save();
		System.out.println(dump);
	}
	

	public void loadConfig(String yaml_file) {
		LOG.info("Trying config found at: " + yaml_file);
		// Attempt to parse the given config file.
		try {
			Yaml yaml = getYaml();
			InputStream input = new FileInputStream(new File(yaml_file));
			LOG.info("Using config found at: " + yaml_file);
			PaintConfig config = (PaintConfig) yaml.load(input);
		} catch (FileNotFoundException e) {
			LOG.info("Failure with config file at: " + yaml_file);
		}
	}

	public Yaml getYaml() {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumperOptions.setIndent(4);
		Representer representer = new ColorRepresenter();
		Constructor constructor = new ColorConstructor();
		Yaml yaml = new Yaml(constructor, representer, dumperOptions);
		return yaml;
	}

	static class ColorConstructor extends Constructor {

		public ColorConstructor() {
			super(PaintConfig.class);
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
}
