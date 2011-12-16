function checkpeers ()
   print("Entered checkpeers")
   local l = tcf.peer_server_list()
   for i,peer in pairs(l) do
      print(i, peer)
   end
   print()
   tcf.post_event(checkpeers, 1000000)
end

tcf.post_event(checkpeers)
