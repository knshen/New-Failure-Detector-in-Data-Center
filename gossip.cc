#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <cstdio>
#include <boost/algorithm/string.hpp>
#include <utility>
#include <set>

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/global-route-manager.h"
//#include "ns3/mobility-module.h"
#include "ns3/netanim-module.h"
#include "ns3/assert.h"
#include "ns3/ipv4-global-routing-helper.h"

#define random(x) (rand()%x)  // [0, x)

using namespace std;
using namespace ns3;
using namespace boost;


vector<vector<bool> > readNxNMatrix (std::string adj_mat_file_name);
void saveIP(vector<Ipv4Address> ips);
vector < pair<int, double> > readServerCrashFile(string fileName);
bool isCrashNow(int node_id, double now, vector < pair<int, double> > server_crash);

NS_LOG_COMPONENT_DEFINE ("GossipGenericTopologyCreation");


int main (int argc, char *argv[])
{
	// global configure
	string server_crash_file_name = "/home/knshen/server-crash-1min.txt";
	string fileName = "/home/knshen/topo.txt";
	string rulePath = "/home/knshen/rule.txt";
	uint32_t linkCount = 0;

	Config::SetDefault("ns3::Ipv4GlobalRouting::RespondToInterfaceEvents", BooleanValue(true));
	Config::SetDefault("ns3::Ipv4GlobalRouting::RandomEcmpRouting", BooleanValue(true));

	// read topology file
	vector< vector<bool> > matrix = readNxNMatrix (fileName);
	int num_nodes = matrix.size();

	// create nodes
	NodeContainer nodes;   // Declare nodes objects
	nodes.Create (num_nodes);

	// configure p2p link attributes
	PointToPointHelper p2p;
	p2p.SetDeviceAttribute ("DataRate", StringValue ("100Mbps"));
	p2p.SetChannelAttribute ("Delay", StringValue ("100ms"));

	// install internet stack
	InternetStackHelper internet;
	internet.Install (NodeContainer::GetGlobal ());

	// assign IP address
	Ipv4AddressHelper ipv4_n;
	ipv4_n.SetBase ("10.0.0.0", "255.255.255.252");
	vector<Ipv4Address> ips(num_nodes);

	// create links
	for (size_t i = 0; i < matrix.size (); i++)
	{
		for (size_t j = 0; j < matrix[i].size (); j++)
		{
			if (matrix[i][j] == 1)
			{
				NodeContainer n_links = NodeContainer (nodes.Get (i), nodes.Get (j));
				NetDeviceContainer n_devs = p2p.Install (n_links);
				Ipv4InterfaceContainer ic = ipv4_n.Assign (n_devs);
				ips[i] = ic.GetAddress(0);
				ips[j] = ic.GetAddress(1);
				/*
				if(i < 12)
					cout<<i<<": "<<ips[i]<<endl;
				if(j < 12)
					cout<<j<<": "<<ips[j]<<endl;
				*/
				ipv4_n.NewNetwork ();
				linkCount++;
			}

		}
	}

	Ipv4GlobalRoutingHelper::PopulateRoutingTables ();


	// install server:
	vector<ApplicationContainer> serverApps; // size = num_nodes = 132
	UdpServerHelper echoServer(9999);
	for (int i = 0; i < num_nodes; i++)
	{
		ApplicationContainer sa = echoServer.Install (nodes.Get(i));
		sa.Start (Seconds (1.0));
		sa.Stop(Seconds(86400.0));
		serverApps.push_back(sa);
		//sa.Stop (Seconds (86401.0)); // server run one day
	}

	// install clients:
	double end_time = 60.0;
	double t_gossip = 0.1;
	bool haveCrash = true;
	// server crash:
	vector < pair<int, double> > server_crash = readServerCrashFile(server_crash_file_name);

	for (double time = 2; time <= end_time; time += t_gossip)
	{
		for (int i = 12; i < num_nodes; i++)
		{
			// is crash at this time?
			if(haveCrash && isCrashNow(i, time, server_crash))
				continue;

			// i send message to dest
			int dest = random(120) + 12;
			while(dest == i)	
				dest = random(120) + 12;
			
			//cout<<"dest: "<<dest<<endl;
			UdpClientHelper echoClient(ips[dest], 9999);
			echoClient.SetAttribute ("MaxPackets", UintegerValue (1));
			//echoClient.SetAttribute ("Interval", TimeValue (Seconds (0.1))); // every 100ms
			echoClient.SetAttribute ("PacketSize", UintegerValue (1024)); // 1024 Byte
			ApplicationContainer ac = echoClient.Install(nodes.Get(i));
			ac.Start (Seconds (time));
		}
		
	}

	
	// dump
	p2p.EnablePcapAll ("gossip");
	//AsciiTraceHelper ascii;
	//p2p.EnableAsciiAll (ascii.CreateFileStream ("topo.tr"));
	Simulator::Run ();
	Simulator::Destroy ();

	return 0;
}

bool isCrashNow(int node_id, double now, vector < pair<int, double> > server_crash)
{
	for(uint32_t i=0; i<server_crash.size(); i++)
		if(node_id == server_crash[i].first && now >= server_crash[i].second)
			return true;
	return false;
}


vector < pair<int, double> > readServerCrashFile(string fileName)
{
	vector < pair<int, double> > res;
	ifstream server_crash_file;
	server_crash_file.open(fileName.c_str(), ios::in);

	while (!server_crash_file.eof())
	{
		string line;
		getline(server_crash_file, line);
		if (line == "")
			break;
		vector<string> tokens;
		boost::split(tokens, line, boost::is_any_of(" "));
		int id = atoi(tokens[0].c_str());
		double time = atof(tokens[1].c_str());
		pair<int, double> p(id, time);
		res.push_back(p);
	}

	server_crash_file.close();
	return res;
}

void saveIP(vector<Ipv4Address> ips)
{
	for (uint32_t i = 0; i < ips.size(); i++)
	{
		cout << (12 + i) << ": " << ips[i] << endl;
	}
}

vector< vector<bool> > readNxNMatrix (std::string adj_mat_file_name)
{
	ifstream adj_mat_file;
	adj_mat_file.open (adj_mat_file_name.c_str (), ios::in);
	if (adj_mat_file.fail ())
	{
		NS_FATAL_ERROR ("File " << adj_mat_file_name.c_str () << " not found");
	}
	vector<vector<bool> > array;
	int i = 0;
	int n_nodes = 0;

	while (!adj_mat_file.eof ())
	{
		string line;
		getline (adj_mat_file, line);
		if (line == "")
		{
			NS_LOG_WARN ("WARNING: Ignoring blank row in the array: " << i);
			break;
		}

		istringstream iss (line);
		bool element;
		vector<bool> row;
		int j = 0;

		while (iss >> element)
		{
			row.push_back (element);
			j++;
		}

		if (i == 0)
		{
			n_nodes = j;
		}

		if (j != n_nodes )
		{
			NS_LOG_ERROR ("ERROR: Number of elements in line " << i << ": " << j << " not equal to number of elements in line 0: " << n_nodes);
			NS_FATAL_ERROR ("ERROR: The number of rows is not equal to the number of columns! in the adjacency matrix");
		}
		else
		{
			array.push_back (row);
		}
		i++;
	}

	if (i != n_nodes)
	{
		NS_LOG_ERROR ("There are " << i << " rows and " << n_nodes << " columns.");
		NS_FATAL_ERROR ("ERROR: The number of rows is not equal to the number of columns! in the adjacency matrix");
	}

	adj_mat_file.close ();
	return array;
}