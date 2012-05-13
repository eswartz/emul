import sys, os.path

from twisted.application import internet, service
from twisted.internet import reactor
from twisted.web import static, server
from twisted.web.resource import Resource

class MyResource(Resource):
    def render_GET(self, request):
        return "<html>Hello, world!</html>"

if "github" in "$v9t9.host":
	print "NOTE: the last build is targeting $v9t9.host (-Drelease=1),\nso this script will not work"
	sys.exit(1)
    
root = MyResource()
v9t9 = static.File(os.path.split(os.path.realpath(__file__))[0])

# twisted doesn't like making pathy components without parents;
# just go to the root name
childEl = os.path.split("$v9t9.root")[1]
root.putChild(childEl, v9t9)

application = service.Application('web')
site = server.Site(root)
sc = service.IServiceCollection(application)
i = internet.TCPServer(8080, site)
i.setServiceParent(sc)

print "Connect to $v9t9.host/" + childEl + "/v9t9.html"
reactor.listenTCP(8080, server.Site(root))
reactor.run()

