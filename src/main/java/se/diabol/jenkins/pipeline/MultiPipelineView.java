package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.ArrayList;
import java.util.List;

public class MultiPipelineView extends AbstractPipelineView {

    private List<Component> components;
    private int noOfColumns = 1;

    @DataBoundConstructor
    public MultiPipelineView(String name, int noOfColumns, List<Component> components) {
        super(name);
        this.components = components;
        this.noOfColumns = noOfColumns;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        for (Component component : components) {
            if (component.getFirstJob().equals(oldName)) {
                component.setFirstJob(newName);
            }
        }
    }

    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        List<Pipeline> result = new ArrayList<>();
        for (Component component : components) {
            AbstractProject firstJob = Jenkins.getInstance().getItem(component.getFirstJob(), Jenkins.getInstance(), AbstractProject.class);

            result.add(pipelineFactory.createPipelineLatest(pipelineFactory.extractPipeline(component.getName(), firstJob)));

        }
        return result;
    }


    @Extension
    public static class DescriptorImpl extends PipelineViewDescriptor {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }
    }


    public static class Component extends AbstractDescribableImpl<Component> {
        private String name;
        private String firstJob;

        @DataBoundConstructor
        public Component(String name, String firstJob) {
            this.name = name;
            this.firstJob = firstJob;
        }

        public String getName() {
            return name;
        }

        public String getFirstJob() {
            return firstJob;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFirstJob(String firstJob) {
            this.firstJob = firstJob;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<Component> {
            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
                return ProjectUtil.fillAllProjects(context);
            }

            public FormValidation doCheckName(@QueryParameter String value) {
                if (value != null && !value.trim().equals("")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Please supply a title!");
                }
            }

        }
    }
}