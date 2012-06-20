package aQute.service.flow;

public interface Pipe<In, Out> extends Sink<Out>, Source<In> {

}
