#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <boost/algorithm/string.hpp>

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

using namespace std;
using namespace ns3;
using namespace boost;

vector<vector<bool> > readNxNMatrix (std::string adj_mat_file_name);
void saveIP(vector<Ipv4Address> ips);
vector < vector<int> > readRuleFile(string fileName);

NS_LOG_COMPONENT_DEFINE ("GenericTopologyCreation");

int main (int argc, char *argv[])
{
	// global configure
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
	vector<Ipv4Address> ips;

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
				if (j >= 12)
					ips.push_back(ic.GetAddress(1));
				ipv4_n.NewNetwork ();
				linkCount++;
			}

		}
	}
	//NS_LOG_INFO("# of nodes: " + num_nodes);
	//NS_LOG_INFO("# of links: " + linkCount);
	cout << "# of nodes: " << num_nodes << endl;
	cout << "# of links: " << linkCount << endl;
	cout << "# of servers: " << ips.size() << endl;
	//saveIP(ips);
	// set routing database
	Ipv4GlobalRoutingHelper::PopulateRoutingTables ();

	// install udp application
	// server (receiver)
	UdpServerHelper echoServer(9999);
	for (int i = 0; i < num_nodes; i++)
	{
		ApplicationContainer serverApps = echoServer.Install (nodes.Get(i));
		serverApps.Start (Seconds (1.0));
		serverApps.Stop (Seconds (1000.0));
	}

	vector< vector<int> > masters = readRuleFile(rulePath);

	// client (sender)
	for(int i=12; i<=131; i++)
	{
		// for each server k = 6
		vector<UdpClientHelper> apps;
		for(uint32_t j=0; j<masters[i-12].size(); j++)
		{
			UdpClientHelper echoClient(ips[masters[i-12][j]-12], 9999);
			echoClient.SetAttribute ("MaxPackets", UintegerValue (100));
			echoClient.SetAttribute ("Interval", TimeValue (Seconds (0.1)));
			echoClient.SetAttribute ("PacketSize", UintegerValue (1024));
			apps.push_back(echoClient);
		}
		for (uint32_t j = 0; j<apps.size(); j++)
		{
			ApplicationContainer ac = apps[j].Install(nodes.Get(i));
			ac.Start (Seconds (2.0));
			ac.Stop (Seconds (22.0));
		}
	
	}
	
	// dump
	p2p.EnablePcapAll ("topo");
	//AsciiTraceHelper ascii;
	//p2p.EnableAsciiAll (ascii.CreateFileStream ("topo.tr"));
	Simulator::Run ();
	Simulator::Destroy ();

	return 0;
}

void saveIP(vector<Ipv4Address> ips)
{
	for (uint32_t i = 0; i < ips.size(); i++)
	{
		cout << (12 + i) << ": " << ips[i] << endl;
	}
}

vector < vector<int> > readRuleFile(string fileName)
{
	vector < vector<int> > res;
	ifstream rule_file;
	rule_file.open (fileName.c_str (), ios::in);

	while (!rule_file.eof())
	{
		vector<int> tmp;
		string line;
		getline(rule_file, line);
		vector<string> tokens;
		vector<string> masters;
		if (line == "")
			break;
		boost::split(tokens, line, boost::is_any_of(":"));
		boost::split(masters, tokens[1], boost::is_any_of(" "));
		//cout << tokens[1] << endl;
		for (uint32_t i = 0; i < masters.size(); i++)
			tmp.push_back(atoi(masters[i].c_str()));

		res.push_back(tmp);
	}

	rule_file.close ();
	return res;
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