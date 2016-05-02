/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.eclipse.runtime;

public abstract class AbstractRuntime implements IRuntime {

	/**
	 * Added because of https://issues.jboss.org/browse/DROOLS-1154 This class
	 * parses a version string into its component "major number", "minor number"
	 * , "patch level" and an optional "build name". Major and minor must be
	 * non-negative integers. The patch number may be a lower- or upper-case "x"
	 * which is interpreted as "any patch level"; internally this is represented
	 * as an integer value of -1. The build name can be any text, including "."
	 * characters. For example these are all valid version numbers:
	 * 
	 * 6.4.0
	 * 6.4.0.Final
	 * 6.4.0.SNAPSHOT-1.0.0
	 * 6.4.x
	 * 6.4.x.Final
	 */
	public static class Version implements Comparable<Version> {
		int major;
		int minor;
		int patch;
		String build;
		
		public Version(String version) {
			this();
			if (validate(version)==null) {
				String parts[] = version.split("\\.");
				major = Integer.parseInt(parts[0]);
				minor = Integer.parseInt(parts[1]);
				if (parts[2].equalsIgnoreCase("X"))
					patch = -1;
				else
					patch = Integer.parseInt(parts[2]);
				for (int i=3; i<parts.length; ++i) {
					if (build==null)
						build = parts[i];
					else
						build = build + "." + parts[i];
				}
				if (build!=null && build.isEmpty())
					build = null;
			}
		}
		
		public Version() {
			// create an empty, invalid Version
			major = -1;
			minor = -1;
			patch = -2;
			build = null;
		}

		public boolean isValid() {
			return major>=0 && minor>=0 && patch >= -1;
		}
		
		public static String validate(String version) {
			int major = -1;
			int minor = -1;
			int patch = -2;
			if (version==null || version.isEmpty()) {
				return "Version may not be empty";
			}
			String parts[] = version.split("\\.");
			if (parts.length < 3) {
				return "Version must be in the form major#.minor#.patch#[.build-name]";
			} else {
				try {
					major = Integer.parseInt(parts[0]);
					minor = Integer.parseInt(parts[1]);
					if (parts[2].equalsIgnoreCase("X"))
						patch = -1;
					else
						patch = Integer.parseInt(parts[2]);
				} catch (Exception e) {
				}
				if (major<0) {
					return "Version major number is invalid";
				} else if (minor<0) {
					return "Version minor number is invalid";
				} else if (patch<-1) {
					return "Version patch number is invalid";
				}
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (obj instanceof Version) {
				Version that = (Version) obj;
				if (this.major == that.major &&
						this.minor == that.minor &&
						(this.patch==-1 || that.patch==-1 || this.patch==that.patch)) {
					// so far, so good. compare build names
					if (this.build==null && that.build==null)
						return true;
					if (this.build!=null && this.build.equals(that.build))
						return true;
				}
				return false;
			}
			return obj.toString().equals(this.toString());
		}

		public boolean compatible(Version that) {
			return this.major == that.major &&
					this.minor == that.minor &&
					(this.patch==-1 || that.patch==-1 || this.patch==that.patch);
		}

		@Override
		public int compareTo(Version that) {
			int i = this.major - that.major;
			if (i==0) {
				i = this.minor - that.minor;
				if (i==0) {
					if (this.patch == -1)
						return -1;
					if (that.patch == -1)
						return 1;
					i = this.patch - that.patch;
				}
			}
			return i;
		}

		@Override
		public String toString() {
			String version = ""
					+ (major<0 ? "z" : major) + "."
					+ (minor<0 ? "y" : minor) + "."
					+ (patch<0 ? "x" : patch);
			if (build!=null && !build.isEmpty())
				version += "." + build;
			return version;
		}
		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

		public int getPatch() {
			return patch;
		}

		public String getBuild() {
			return build;
		}
	}

	protected Version version = new Version();
	protected String name;
	protected String path;
	protected boolean isDefault;
	protected String[] jars;
	
	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = new Version(version);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public boolean isDefault() {
		return isDefault;
	}
	
	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public String[] getJars() {
		return jars;
	}

	@Override
	public void setJars(String[] jars) {
		this.jars = jars;
	}
    
    public String toString() {
    	return getName();
    }

	abstract public String getProduct();
	
	public void setProduct(String string) {
	}
	
	public String getId() {
		return createRuntimeId(getProduct(), getVersion().toString());
	}

    public static String createRuntimeId(String product, String version) {
    	return product + "_" + version;
    }

    public static String getProductFromId(String runtimeId) {
    	int i = runtimeId.lastIndexOf("_");
    	if (i>0) {
    		return runtimeId.substring(0,i);
    	}
    	return null;
    }

    public static String getVersionFromId(String runtimeId) {
    	int i = runtimeId.lastIndexOf("_");
    	if (i>0) {
    		return runtimeId.substring(i+1);
    	}
    	return null;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (obj==this)
			return true;
		if (obj==null)
			return false;
		if (obj instanceof IRuntime) {
			IRuntime that = ((IRuntime)obj);
			if (this.getProduct().equals(that.getProduct()) && this.getVersion().equals(that.getVersion())) {
				return true;
			}
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(IRuntime that) {
		int i = getProduct().compareTo(that.getProduct());
		if (i==0) {
			i = getVersion()==null ? -1 : getVersion().toString().compareTo(that.getVersion().toString());
		}
		return i;
	}
}
