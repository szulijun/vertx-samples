package backup

import groovy.json.JsonOutput
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.EventBus

/**
 * This is a worker verticle. The backup reads the MongoDB invites collection from MongoDB and writes it as a backup
 * to a file. Since this can take some time we do not want to block one of the threads that handle incoming requests.
 * Therefore we make a worker verticle out of this one.
 *
 * @author Jettro Coenradie
 */

def log = container.logger

EventBus eventBus = vertx.eventBus

eventBus.registerHandler("message.backup.create") {message ->
    log.info "Start creating the backup and send a reply message that the backup is created."

    eventBus.send("vertx.persist", ["action": "find", "collection": "invites", "matcher": [:]]) { reply ->
        log.info reply.body
        def buffer = new Buffer()
        buffer.appendString(JsonOutput.prettyPrint(JsonOutput.toJson(reply.body)))
        vertx.fileSystem.writeFileSync("backup.txt", buffer)
        message.reply(["message": "We have created the backup"])
    }
}



log.info "This is a worker module"