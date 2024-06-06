package persiana.code;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;


public class RestServer extends AbstractVerticle {

	private Map<Integer, SensorEntity> sensor = new HashMap<Integer, SensorEntity>();
	private Map<Integer, ActuadorEntity> actuador = new HashMap<Integer, ActuadorEntity>();
	private Gson gson;
	

	

	
	
	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		createSomeDataSensor(25);
		createSomeDataActuador(25);

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api/sensor*").handler(BodyHandler.create());
		router.get("/api/sensor").handler(this::getAllWithParamsSensor);
		router.get("/api/sensor/:idSensor").handler(this::getOneSensor);
		router.post("/api/sensor/new").handler(this::addOneSensor);
		router.delete("/api/sensor/:idSensor").handler(this::deleteOneSensor);
		router.put("/api/sensor/:idSensor").handler(this::putOneSensor);
		router.get("/api/sensor/groups/:idGroup").handler(this::getSensorsByGroup);
		router.get("/api/sensor/groups/time/:idGroup").handler(this::getAllGroupsTiempoSensor);
		
		router.get("/api/actuador").handler(this::getAllWithParamsActuador);
		router.get("/api/actuador/:idActuador").handler(this::getOneActuador);
		router.post("/api/actuador/new").handler(this::addOneActuador);
		router.delete("/api/actuador/:idActuador").handler(this::deleteOneActuador);
		router.put("/api/actuador/:idActuador").handler(this::putOneActuador);
		router.get("/api/actuador/groups/:idGroup").handler(this::getActuadorByGroup);
		router.get("/api/actuador/groups/time/:idGroup").handler(this::getAllGroupsTiempoActuador);
	}

	@SuppressWarnings("unused")
	private void getAllSensor(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorEntityListWrapper(sensor.values())));
	}

	private void getAllWithParamsSensor(RoutingContext routingContext) {
		final String idSensor = routingContext.queryParams().contains("idSensor") ? routingContext.queryParam("idSensor").get(0) : null;
		//final String username = routingContext.queryParams().contains("username") ? routingContext.queryParam("username").get(0) : null;
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorEntityListWrapper(sensor.values().stream().filter(elem -> {
					boolean res = true;
					res = res && idSensor != null ? elem.idSensor.equals(idSensor) : true;
					return res;
				}).collect(Collectors.toList()))));
	}

	private void getOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idSensor"));
		if (sensor.containsKey(id)) {
			SensorEntity ds = sensor.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneSensor(RoutingContext routingContext) {
		final SensorEntity user = gson.fromJson(routingContext.getBodyAsString(), SensorEntity.class);
		sensor.put(user.getIdSensor(), user);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(user));
	}

	private void deleteOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idSensor"));
		if (sensor.containsKey(id)) {
			SensorEntity user = sensor.get(id);
			sensor.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idSensor"));
		SensorEntity ds = sensor.get(id);
		final SensorEntity element = gson.fromJson(routingContext.getBodyAsString(), SensorEntity.class);
		ds.setIdPlaca(element.getIdPlaca());
		ds.setTimeStamp(element.getTimeStamp());
		ds.setTemperatura(element.getTemperatura());
		ds.setIdGroup(element.getIdGroup());
		sensor.put(ds.getIdSensor(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	private void getSensorsByGroup(RoutingContext routingContext) {
		int id=Integer.parseInt(routingContext.request().getParam("idGroup"));
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new SensorEntityListWrapper(sensor.values()).getsensorList().stream().filter(x->x.getIdGroup()==id).collect(Collectors.toList())));
		
	}
	private void getAllGroupsTiempoSensor(RoutingContext routingContext) {
		Integer idgrupo = Integer.parseInt(routingContext.request().getParam("IdGroup"));
		
		Optional<SensorEntity> res = sensor.values().stream().filter(x->x.getIdGroup()==idgrupo).max(Comparator.comparing(SensorEntity::getTimeStamp));
		String json = res.isPresent() ? gson.toJson(res.get()) : "";
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(json));
	}
	

	private void createSomeDataSensor(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			long lg=rnd.nextLong();
			int idgroup=1;
			if(id%2==0) {
				idgroup=2;
			}
			sensor.put(id, new SensorEntity(id,id,lg,id,idgroup));
		});
	}

	
/////////////ACTUADOR
	
	@SuppressWarnings("unused")
	private void getAllActuador(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new ActuadorEntityListWrapper(actuador.values())));
	}

	private void getAllWithParamsActuador(RoutingContext routingContext) {
		final String grados = routingContext.queryParams().contains("grados") ? routingContext.queryParam("grados").get(0) : null;
		final String idActuador = routingContext.queryParams().contains("idActuador") ? routingContext.queryParam("idActuador").get(0) : null;
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new ActuadorEntityListWrapper(actuador.values().stream().filter(elem -> {
					boolean res = true;
					res = res && grados != null ? elem.getGrados().equals(grados) : true;
					res = res && idActuador != null ? elem.getIdActuador().equals(idActuador) : true;
					return res;
				}).collect(Collectors.toList()))));
	}

	private void getOneActuador(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idActuador"));
		if (actuador.containsKey(id)) {
			ActuadorEntity ds = actuador.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneActuador(RoutingContext routingContext) {
		final ActuadorEntity user = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);
		actuador.put(user.getIdActuador(), user);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(user));
	}

	private void deleteOneActuador(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idActuador"));
		if (actuador.containsKey(id)) {
			ActuadorEntity user = actuador.get(id);
			actuador.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOneActuador(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idActuador"));
		ActuadorEntity ds = actuador.get(id);
		final ActuadorEntity element = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);
		ds.setIdPlaca(element.getIdPlaca());
		ds.setEstado(element.getEstado());
		ds.setGrados(element.getGrados());
		ds.setTimeStamp(element.timeStamp);
		ds.setIdGroup(element.idGroup);
		actuador.put(ds.idActuador, ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	
	
	private void getAllGroupsTiempoActuador(RoutingContext routingContext) {
		Integer idgrupo = Integer.parseInt(routingContext.request().getParam("IdGroup"));
		
		Optional<ActuadorEntity> res = actuador.values().stream().filter(x->x.getIdGroup()==idgrupo).max(Comparator.comparing(ActuadorEntity::getTimeStamp));
		String json = res.isPresent() ? gson.toJson(res.get()) : "";
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(json));
	}
	

	
	private void getActuadorByGroup(RoutingContext routingContext) {
		int id=Integer.parseInt(routingContext.request().getParam("idGroup"));
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new ActuadorEntityListWrapper(actuador.values()).getactuadorList().stream().filter(x->x.getIdGroup()==id).collect(Collectors.toList())));
		
	}
	private void createSomeDataActuador(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			long lg=rnd.nextLong();
			int idGroup=1;
			if(id%2==0) {
				idGroup=2;
			}
			actuador.put(id, new ActuadorEntity(id,id,true,id,lg,idGroup));
		});
	}

}
