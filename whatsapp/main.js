const { Client, LocalAuth, MessageMedia } = require('whatsapp-web.js');
const qrcode = require('qrcode');
const fs = require('fs');
const http = require('http');

const dataPath = 'userData';

const client = new Client({
    authStrategy: new LocalAuth({
        dataPath: dataPath
    })
});

console.log("Starting Client");

function updateStatus(status) {
    fs.writeFileSync('status.txt', status, 'utf8');
}

function deleteUserData() {
    if (fs.existsSync(dataPath)) {
        try {
            fs.rmSync(dataPath, { recursive: true, force: true });
            console.log('User data deleted');
        } catch (err) {
            console.error('Failed to delete user data:', err);
        }
    } else {
        console.log('User data directory does not exist.');
    }
}

client.on('ready', async () => {
    console.log('Client is ready with ID: ' + client.info.wid._serialized);
    updateStatus('connected');

    // Replace 'Group Name' with the exact name of your WhatsApp group
    const groupName = 'MySpace';

    // Fetch all chats
    const chats = await client.getChats();

    // Find the group chat by its name
    const groupChat = chats.find(chat => chat.isGroup && chat.name === groupName);

    if (groupChat) {
        console.log(`Chat ID for group '${groupName}': ${groupChat.id._serialized}`);
    } else {
        console.log(`Group '${groupName}' not found.`);
    }


});

client.on('authenticated', () => {
    console.log('Client is authenticated!');
});

client.on('disconnected', (reason) => {
    console.log('Client is disconnected!', reason);
    if (reason === "LOGOUT") {
        console.log('Deleting client session');
        deleteUserData();
    }
    updateStatus('disconnected');
});

client.on('auth_failure', () => {
    console.log('Client has failed authentication');
    updateStatus('disconnected');
});

client.on('qr', qr => {
    qrcode.toFile('qrcode.png', qr, function (err) {
        if (err) throw err;
        console.log('QR code saved as qrcode.png in the current directory');
    });
    updateStatus('disconnected');
});

// Initialize the client
client.initialize();
console.log("Client initialized!");

// Function to send a test message
function sendTestMessage(customMessage, recipientId) {
    if (client.info && client.info.wid) {
        client.sendMessage(recipientId, customMessage).then(response => {
            console.log('Test message sent successfully', response);
        }).catch(err => {
            console.error('Failed to send test message', err);
        });
    } else {
        console.log('Client is not ready to send messages');
    }
}

// Function to get the contact ID by name
async function getContactID(contactName) {
    const contacts = await client.getContacts();
    const contact = contacts.find(contact => contact.pushname === contactName || contact.name === contactName);
    if (contact) {
        return contact.id._serialized;
    } else {
        console.error(`Contact '${contactName}' not found.`);
        return null;
    }
}

// Function to send an item message with image, name, description, and price as a caption
async function sendItemMessage(item, recipientContactName) {
    if (client.info && client.info.wid) {
        // Get the recipient's ID using the contact name
        const recipientId = await getContactID(recipientContactName);

        if (recipientId) {
            // Create the caption with the item details
            const caption = `${item.name}\n${item.description}\n${item.price}`;

            // Create a MessageMedia object from the base64 image data
            const media = new MessageMedia(item.imageMimeType, item.imageData, item.imageFilename);

            // Send the image with the caption
            client.sendMessage(recipientId, media, { caption: caption })
                .then(response => {
                    console.log(`Item message sent successfully with name: ${item.name}`);
                })
                .catch(err => {
                    console.error('Failed to send item image', err);
                });
        }
    } else {
        console.log('Client is not ready to send item messages');
    }
}




// HTTP server to handle requests from JavaFX application
const hostname = '127.0.0.1';
const port = 3000;

const server = http.createServer((req, res) => {
    if (req.method === 'POST' && req.url === '/sendTest') {
        let body = '';

        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            const { customMessage, recipientId } = JSON.parse(body);
            sendTestMessage(customMessage, recipientId);
            res.statusCode = 200;
            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify({ status: 'Test message sent' }));
        });
    } else if (req.method === 'POST' && req.url === '/sendItem') {
        let body = '';

        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            const item = JSON.parse(body);
            sendItemMessage(item, item.recipientId);
            res.statusCode = 200;
            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify({ status: 'Item message sent' }));
        });
    } else {
        res.statusCode = 404;
        res.end();
    }
});

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});