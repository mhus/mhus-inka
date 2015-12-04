package de.mhus.aqua.api;


public interface IRequestProcessor {

	void processRequest(AquaRequest request) throws Exception;

	void processHeadRequest(AquaRequest request) throws Exception;

}
