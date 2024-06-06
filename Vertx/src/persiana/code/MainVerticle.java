	package persiana.code;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle {
	private Gson gson;
	MySQLPool mySqlClient;
	MqttClient mqttClient;

	public void start(Promise<Void> startFuture) {
		
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		
		mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "localhost", s -> {
			mqttClient.publish("topic_1", Buffer.buffer("mqtt funciona"), MqttQoS.AT_LEAST_ONCE, false, false);
		});
		
		
		
		
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dad").setUser("dad").setPassword("dad");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		
		Router router = Router.router(vertx);
		

		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});
		

		router.route("/api/sensor*").handler(BodyHandler.create());
		router.get("/api/sensor").handler(this::getAllSensorsWithConnection);
		router.get("/api/sensor/all").handler(this::getAllSensors);
		router.get("/api/sensor/:idSensor").handler(this::getSensorById);
		router.get("/api/sensor/:idSensor/last").handler(this::getLastSensorId);
	    router.get("/api/sensor/:idGroup/group").handler(this::getLastIdGroupSensor);
		router.post("/api/sensor").handler(this::addSensor);
		router.delete("/api/sensor/:idSensor").handler(this::deleteSensor);
		router.put("/api/sensor/:idSensor").handler(this::updateSensor);
		router.post("/api/sensor/dht").handler(this::postDht11);

		router.route("/api/actuador*").handler(BodyHandler.create());
		router.get("/api/actuador").handler(this::getAllActuadoresWithConnection);
		router.get("/api/actuador/all").handler(this::getAllActuadores);
		router.get("/api/actuador/:idActuador").handler(this::getActuadorById);
		router.get("/api/actuador/:idActuador/last").handler(this::getLastActuadorId);
		router.get("/api/actuador/:idGroup/group").handler(this::getLastIdGroupActuador);
		router.post("/api/actuador").handler(this::addActuador);
		router.delete("/api/actuador/:idActuador").handler(this::deleteActuador);
		router.put("/api/actuador/:idActuador").handler(this::updateActuador);
		router.post("/api/actuador/servo").handler(this::postServo);
	}



	public void stop(Promise<Void> stopPromise) throws Exception {
		try {
			stopPromise.complete();
		} catch (Exception e) {
			stopPromise.fail(e);
		}
		super.stop(stopPromise);
	}

	// Sensor Endpoints
	private void getAllSensors(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM sensor;").execute(res -> {
			if (res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				List<List<Object>> result = new ArrayList<>();
				for (Row elem : resultSet) {
					List<Object> sensorData = new ArrayList<>();
					sensorData.add(elem.getInteger("idSensor"));
					sensorData.add(elem.getInteger("idPlaca"));
					sensorData.add(elem.getLong("timestamp"));
					sensorData.add(elem.getInteger("temperatura"));
					sensorData.add(elem.getInteger("idGroup"));
					result.add(sensorData);
				}
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(result.toString());
			} else {
				routingContext.response().setStatusCode(500)
						.end("Error al obtener los sensores: " + res.cause().getMessage());
			}
		});
	}

	private void getAllSensorsWithConnection(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM sensor;").execute(res -> {
					if (res.succeeded()) {
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<SensorEntity> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new SensorEntity(elem.getInteger("idSensor"), elem.getInteger("idPlaca"),
									elem.getLong("timeStamp"),elem.getInteger("temperatura"), 
									elem.getInteger("idGroup")));
						}
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(200).end(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
						routingContext.response().setStatusCode(500).end("Error al obtener los sensores: " + res.cause().getMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
			}
		});
	}

	private void getSensorById(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM sensor WHERE idSensor = ?").execute(Tuple.of(idSensor),
						res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<SensorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new SensorEntity(elem.getInteger("idSensor"), elem.getInteger("idPlaca"),
											elem.getLong("timeStamp"),elem.getInteger("temperatura"), 
											elem.getInteger("idGroup")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al obtener los sensores: " + res.cause().getMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getLastSensorId(RoutingContext routingContext) {
	    Integer idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM sensor WHERE idSensor = ? ORDER BY timestamp DESC LIMIT 1")
	                    .execute(Tuple.of(idSensor), res -> {
	                        if (res.succeeded()) {
	                            RowSet<Row> resultSet = res.result();
	                            List<SensorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new SensorEntity(elem.getInteger("idSensor"), elem.getInteger("idPlaca"),
											elem.getLong("timeStamp"),elem.getInteger("temperatura"), 
											elem.getInteger("idGroup")));
								}
				                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(gson.toJson(result));
	                        } else {
	                            System.out.println("Error: " + res.cause().getLocalizedMessage());
	                            routingContext.response()
	                                    .setStatusCode(404)
	                                    .end("Error al obtener el sensor con idSensor " + idSensor + ": " + res.cause().getMessage());
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            System.out.println(connection.cause().toString());
	            routingContext.response()
	                    .setStatusCode(500)
	                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
	        }
	    });
	}

	private void getLastIdGroupSensor(RoutingContext routingContext) {
	    int idGroup = Integer.parseInt(routingContext.request().getParam("idGroup"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM sensor WHERE idGroup = ? ORDER BY timestamp DESC LIMIT 1")
	                    .execute(Tuple.of(idGroup), res -> {
	                        if (res.succeeded()) {
	                            RowSet<Row> resultSet = res.result();
	                            List<SensorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new SensorEntity(elem.getInteger("idSensor"), elem.getInteger("idPlaca"),
											elem.getLong("timeStamp"),
											elem.getInteger("temperatura"), elem.getInteger("idGroup")));
								}
				                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(gson.toJson(result));
	                        } else {
	                            System.out.println("Error: " + res.cause().getLocalizedMessage());
	                            routingContext.response()
	                                    .setStatusCode(404)
	                                    .end("Error al obtener el sensor con idGroup " + idGroup + ": " + res.cause().getMessage());
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            System.out.println(connection.cause().toString());
	            routingContext.response()
	                    .setStatusCode(500)
	                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
	        }
	    });
	}
	
	private void addSensor(RoutingContext routingContext) {

	    // Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_Entity
	    final SensorEntity sensor = gson.fromJson(routingContext.getBodyAsString(),
	            SensorEntity.class);

	    // Ejecutamos la inserción en la base de datos MySQL
	    mySqlClient
	            .preparedQuery(
	                    "INSERT INTO sensor (idSensor, idPlaca, timeStamp, temperatura, idGroup) VALUES (?, ?, ?, ?, ?)")
	            .execute((Tuple.of(sensor.getIdSensor(), sensor.getIdPlaca(), sensor.getTimeStamp(),
	                    sensor.getTemperatura(), sensor.getIdGroup())), res -> {
	                        if (res.succeeded()) {
	                            // Si la inserción es exitosa, respondemos con el sensor creado
	                            routingContext.response().setStatusCode(201).putHeader("content-type",
	                                    "application/json; charset=utf-8").end("Sensor añadido correctamente");

	                            // Publicar en MQTT después de la inserción exitosa
	                            if (sensor.getTemperatura() > 25) {
	                                mqttClient.publish(sensor.getIdGroup() + "",
	                                        Buffer.buffer("ON"), MqttQoS.AT_LEAST_ONCE, false, false);
	                            } else {
	                                mqttClient.publish(sensor.getIdGroup() + "",
	                                        Buffer.buffer("OFF"), MqttQoS.AT_LEAST_ONCE, false, false);
	                            }
	                        } else {
	                            // Si hay un error en la inserción, respondemos con el mensaje de error
	                            System.out.println("Error: " + res.cause().getLocalizedMessage());
	                            routingContext.response().setStatusCode(500).end("Error al añadir el sensor: " + res.cause().getMessage());
	                        }
	                    });

	}


	private void deleteSensor(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("DELETE FROM sensor WHERE idSensor = ?").execute(Tuple.of(idSensor),
						res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<SensorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new SensorEntity(elem.getInteger("idSensor"), elem.getInteger("idPlaca"),
											elem.getLong("timeStamp"),
											elem.getInteger("temperatura"), elem.getInteger("idGroup")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());

							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al eliminar el sensor: " + res.cause().getMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
			}
		});
	}


	private void updateSensor(RoutingContext routingContext) {
		// Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
		int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));

		// Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
		final SensorEntity updatedSensor = gson.fromJson(routingContext.getBodyAsString(),
				SensorEntity.class);

		// Ejecutamos la actualización en la base de datos MySQL
		mySqlClient
				.preparedQuery(
						"UPDATE sensor SET idPlaca = ?, timeStamp = ?, temperatura = ?, idGroup = ? WHERE idSensor = ?")
				.execute((Tuple.of(updatedSensor.getIdPlaca(), updatedSensor.getTimeStamp(),
						updatedSensor.getTemperatura(), updatedSensor.getIdGroup(), idSensor)), res -> {
							if (res.succeeded()) {
								// Si la actualización es exitosa, respondemos con el sensor actualizado
								if (res.result().rowCount() > 0) {
									routingContext.response().setStatusCode(200)
											.putHeader("content-type", "application/json; charset=utf-8")
											.end(gson.toJson(updatedSensor));
								}
							} else {
								// Si hay un error en la actualización, respondemos con el código 500
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al actualizar el sensor: " + res.cause().getMessage());
							}
						});
	}

	// Actuador Endpoints

	private void getAllActuadores(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM actuador;").execute(res -> {
			if (res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<ActuadorEntity> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new ActuadorEntity(elem.getInteger("idActuador"),
							elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
							elem.getLong("timeStamp"),elem.getInteger("idGroup")));
				}
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(result.toString());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
				routingContext.response().setStatusCode(500).end("Error al obtener los actuadores: " + res.cause().getMessage());
			}
		});
	}

	private void getAllActuadoresWithConnection(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM actuador;").execute(res -> {
					if (res.succeeded()) {
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<ActuadorEntity> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new ActuadorEntity(elem.getInteger("idActuador"),
									elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
									elem.getLong("timeStamp"),elem.getInteger("idGroup")));
						}
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(200).end(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
						routingContext.response().setStatusCode(500).end("Error al obtener los actuadores: " + res.cause().getMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la conexión: " + connection.cause().getMessage());
			}
		});
	}

	private void getActuadorById(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM actuador WHERE idActuador = ?")
						.execute(Tuple.of(idActuador), res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<ActuadorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new ActuadorEntity(elem.getInteger("idActuador"),
											elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
											elem.getLong("timeStamp"),elem.getInteger("idGroup")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al obtener el actuador: " + res.cause().getMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getLastActuadorId(RoutingContext routingContext) {
	    Integer idSensor = Integer.parseInt(routingContext.request().getParam("idActuador"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM actuador WHERE idActuador = ? ORDER BY timestamp DESC LIMIT 1")
	                    .execute(Tuple.of(idSensor), res -> {
	                        if (res.succeeded()) {
	                            RowSet<Row> resultSet = res.result();
	                            List<ActuadorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new ActuadorEntity(elem.getInteger("idActuador"),
											elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
											elem.getLong("timeStamp"),elem.getInteger("idGroup")));
								}
				                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(gson.toJson(result));
	                        } else {
	                            System.out.println("Error: " + res.cause().getLocalizedMessage());
	                            routingContext.response()
	                                    .setStatusCode(404)
	                                    .end("Error al obtener el actuador con idActuador " + idSensor + ": " + res.cause().getMessage());
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            System.out.println(connection.cause().toString());
	            routingContext.response()
	                    .setStatusCode(500)
	                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
	        }
	    });
	}

	private void getLastIdGroupActuador(RoutingContext routingContext) {
	    int idGroup = Integer.parseInt(routingContext.request().getParam("idGroup"));
	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            connection.result().preparedQuery("SELECT * FROM actuador WHERE idGroup = ? ORDER BY timestamp DESC LIMIT 1")
	                    .execute(Tuple.of(idGroup), res -> {
	                        if (res.succeeded()) {
	                            RowSet<Row> resultSet = res.result();
	                            List<ActuadorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new ActuadorEntity(elem.getInteger("idActuador"),
											elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
											elem.getLong("timeStamp"),elem.getInteger("idGroup")));
								}
				                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(gson.toJson(result));
	                        } else {
	                            System.out.println("Error: " + res.cause().getLocalizedMessage());
	                            routingContext.response()
	                                    .setStatusCode(404)
	                                    .end("Error al obtener el actuador con idGroup " + idGroup + ": " + res.cause().getMessage());
	                        }
	                        connection.result().close();
	                    });
	        } else {
	            System.out.println(connection.cause().toString());
	            routingContext.response()
	                    .setStatusCode(500)
	                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
	        }
	    });
	}	


	private void addActuador(RoutingContext routingContext) {

		// Parseamos el cuerpo de la solicitud HTTP a un objeto Actuador_Entity
		final ActuadorEntity actuador = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);

		// Ejecutamos la inserción en la base de datos MySQL
		mySqlClient.preparedQuery(
				"INSERT INTO actuador (idActuador,idPlaca, estado, grados,timeStamp, idGroup) VALUES (?, ?, ?, ?, ?, ?)")
				.execute((Tuple.of(actuador.getIdActuador(), actuador.getIdPlaca(), actuador.getEstado(),
						actuador.getGrados(), actuador.getTimeStamp(), actuador.getIdGroup())), res -> {
							if (res.succeeded()) {
								// Si la inserción es exitosa, respondemos con el actuador creado
								routingContext.response().setStatusCode(201).putHeader("content-type",
										"application/json; charset=utf-8").end("Acutador añadido correctamente");
							} else {
								// Si hay un error en la inserción, respondemos con el mensaje de error
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al añadir el actuador: " + res.cause().getMessage());
							}
						});
		
	}

	private void deleteActuador(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("DELETE FROM actuador WHERE idActuador = ?")
						.execute(Tuple.of(idActuador), res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<ActuadorEntity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new ActuadorEntity(elem.getInteger("idActuador"),
											elem.getInteger("idPlaca"), elem.getBoolean("estado"), elem.getInteger("grados"),
											elem.getLong("timeStamp"),elem.getInteger("idGroup")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());

							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al eliminar el actuador: " + res.cause().getMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
			}
		});
	}


	private void updateActuador(RoutingContext routingContext) {
		// Obtenemos el ID del actuador de los parámetros de la solicitud HTTP
		int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));

		// Obtenemos el actuador actualizado del cuerpo de la solicitud HTTP
		final ActuadorEntity updatedActuador = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);

		// Ejecutamos la actualización en la base de datos MySQL
		mySqlClient
				.preparedQuery(
						"UPDATE actuador SET idPlaca = ?, estado = ?, grados = ?, timeStamp = ?, idGroup = ? WHERE idActuador = ?")
				.execute((Tuple.of(updatedActuador.getIdPlaca(), updatedActuador.getEstado(),
						updatedActuador.getGrados(), idActuador)), res -> {
							if (res.succeeded()) {
								// Si la actualización es exitosa, respondemos con el actuador actualizado
								if (res.result().rowCount() > 0) {
									routingContext.response().setStatusCode(200)
											.putHeader("content-type", "application/json; charset=utf-8")
											.end(gson.toJson(updatedActuador));
								}
							} else {
								// Si hay un error en la actualización, respondemos con el código 500 
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().setStatusCode(500).end("Error al actualizar el actuador: " + res.cause().getMessage());

							}
						});
	}
	
	
	
	
	private void postDht11(RoutingContext routingContext) {
	    final SensorEntity dht11 = gson.fromJson(routingContext.getBodyAsString(), SensorEntity.class);
	    dht11.setTimeStamp(Calendar.getInstance().getTimeInMillis());
	    mySqlClient.getConnection(connection -> {
	        if (!(connection.succeeded())) {
	            System.out.println("Error obteniendo conexión: " + connection.cause().toString());
	            routingContext.response().setStatusCode(500).end();
	        }
	        connection.result().preparedQuery("INSERT INTO sensor(idSensor, idPlaca, timeStamp, temperatura, idGroup) VALUES ('" +
	            	dht11.getIdSensor() + "', '" + dht11.getIdPlaca()  + "', '" + dht11.getTimeStamp()+ "', '" + dht11.getTemperatura()
	                    + "', '" + dht11.getIdGroup()+ "');")
	                    .execute(dht11Res -> {
	                        if (!(dht11Res.succeeded())) {
	                            System.out.println("Error obteniendo datos de alarmas: " + dht11Res.cause().getLocalizedMessage());
	                            routingContext.response().setStatusCode(500).end();
	                            connection.result().close();
	                        }
	                        connection.result().query("SELECT idGroup FROM sensor")
	                                  .execute(placaRes -> {
	                                      if (!(placaRes.succeeded())) {
	                                            System.out.println("Error obteniendo datos de placas: " + placaRes.cause().getLocalizedMessage());
	                                            routingContext.response().setStatusCode(500).end();
	                                            connection.result().close();
	                                        } 
	                                        RowSet<Row> result = placaRes.result();
	                                        String idG = null;
	                                        for (Row placa : result) {
	                                             idG = placa.getInteger("idGroup").toString();
	                                             
	                                        }
	                                        if (idG != null) {
	                                                String valor = dht11.getTemperatura() >= 25 ? "on" : "off";
	                                                mqttClient.publish("dht" + idG, Buffer.buffer(valor), MqttQoS.AT_LEAST_ONCE, false, false);
	                                                routingContext.response().setStatusCode(201)
	                                                        .putHeader("content-type", "application/json; charset=utf-8")
	                                                        .end(gson.toJson(dht11));
	                                            }
	                                            connection.result().close();
	                                            ;});});});
	    }
	
	private void postServo(RoutingContext routingContext) {
	    final ActuadorEntity servo = gson.fromJson(routingContext.getBodyAsString(), ActuadorEntity.class);
	    servo.setTimeStamp(Calendar.getInstance().getTimeInMillis());

	    mySqlClient.getConnection(connection -> {
	        if (!(connection.succeeded())) {
	            System.out.println("Error obteniendo conexión: " + connection.cause().toString());
	            routingContext.response().setStatusCode(500).end();
	        }
	        connection.result().preparedQuery("INSERT INTO actuador(idActuador,idPlaca, estado, grados, timeStamp, idGroup) VALUES ('" +
	        		servo.getIdActuador() + "', '" + servo.getIdPlaca()  + "', '" + (servo.getEstado()? 1 : 0)+ "', '" + servo.getGrados()
	                    + "', '" + servo.getTimeStamp()+ "', '"+ servo.getIdGroup()+ "');")
	                    .execute(servoRes -> {
	                        if (!(servoRes.succeeded())) {
	                            System.out.println("Error obteniendo datos de actuador: " + servoRes.cause().getLocalizedMessage());
	                            routingContext.response().setStatusCode(500).end();
	                            connection.result().close();
	                        }
	                        
	                        routingContext.response().setStatusCode(201)
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .end(gson.toJson(servo));

	                    connection.result().close();
	                });
	        });
	}
}
